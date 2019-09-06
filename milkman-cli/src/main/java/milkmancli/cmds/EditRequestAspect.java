package milkmancli.cmds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.jline.builtins.Commands;

import lombok.RequiredArgsConstructor;
import milkman.ctrl.RequestTypeManager;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.persistence.PersistenceManager;
import milkman.ui.plugin.UiPluginManager;
import milkmancli.AspectCliImportExport;
import milkmancli.CliContext;
import milkmancli.TerminalCommand;
import milkmancli.TerminalUi;

/**
 * Command that clears the screen.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class EditRequestAspect extends TerminalCommand {

	private final RequestTypeManager requestTypeManager;
	private final UiPluginManager plugins;
	private final CliContext context;
	private final PersistenceManager persistence;

	public Void call() throws IOException {
		var reqName = getParameterValue("request");
		var aspectName = tryGetParameterValue("aspect");
		
		RequestContainer req = lookupRequest(reqName);
		
		if (aspectName.isEmpty()) {
			System.out.println("Available aspects to be edited:");
			System.out.println(findEditableAspects(req).stream().map(RequestAspect::getName).collect(Collectors.joining(", ")));
			return null;
		}
		
		
		RequestAspect aspect = req.getAspects().stream()
				.filter(a -> a.getName().equals(aspectName.get()))
				.findAny()
				.orElseThrow();
		AspectCliImportExport aspectEditor =  plugins.loadSpiInstances(AspectCliImportExport.class).stream()
					.filter(e -> e.canHandleAspect(aspect))
					.findAny().orElseThrow();
		
		editAspect(req, aspect, aspectEditor);
		
		context.findWorkspaceOfRequestIdish(reqName).ifPresent(persistence::persistWorkspace);
		
		
		return null;
	}


	private ResponseContainer editAspect(RequestContainer req, RequestAspect aspect, AspectCliImportExport aspectEditor) {
		try {
			File tempFile = File.createTempFile("milkman-"+aspect.getName(), ".txt");
			try(var os = new FileOutputStream(tempFile)){
				IOUtils.write(aspectEditor.exportToString(aspect), os);
			}
			try{
				Commands.nano(TerminalUi.getTerminal(), System.out, System.err, tempFile.toPath().getParent(), new String[] {tempFile.getAbsolutePath()});
			} catch (Exception e) {
				e.printStackTrace();
			}
			try(var is = new FileInputStream(tempFile)){
				String newContent = IOUtils.toString(is);
				aspectEditor.applyNewValue(aspect, newContent);
			}
			tempFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	protected RequestContainer lookupRequest(String reqName) {
		RequestContainer req = context.findRequestByIdish(reqName)
						.orElseThrow(()  -> new IllegalArgumentException("Request not found " + reqName));
		return req;
	}


	public List<RequestAspect> findEditableAspects(RequestContainer request){
		List<AspectCliImportExport> cliImExp = plugins.loadSpiInstances(AspectCliImportExport.class);
		LinkedList<RequestAspect> aspects = new LinkedList<>(request.getAspects());
		aspects.removeIf(a -> cliImExp.stream().noneMatch(cie -> cie.canHandleAspect(a)));
		return aspects;
	}


	@Override
	public String getName() {
		return "edit-request";
	}

	@Override
	public String getAlias() {
		return "e";
	}

	@Override
	public String getDescription() {
		return "Edits an aspect of a request";
	}


	@Override
	public List<Parameter> createParameters() {
		return List.of(
				new Parameter("request", "the name of the request to execute", () -> context.getAvailableRequestNames()),
				new Parameter("aspect", "the aspect to edit", () -> Collections.emptyList(), false));
	}

}