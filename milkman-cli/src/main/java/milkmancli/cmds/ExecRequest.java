package milkmancli.cmds;

import lombok.RequiredArgsConstructor;
import milkman.ctrl.RequestTypeManager;
import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;
import milkman.templater.EnvironmentTemplater;
import milkman.templater.PrefixedTemplaterResolver;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.UiPluginManager;
import milkmancli.AspectCliPresenter;
import milkmancli.CliContext;
import milkmancli.TerminalCommand;
import milkmancli.TerminalUi;
import org.apache.commons.io.IOUtils;
import org.jline.builtins.Commands;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command that clears the screen.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class ExecRequest extends TerminalCommand {

	private final RequestTypeManager requestTypeManager;
	private final UiPluginManager plugins;
	private final CliContext context;

	public Void call() throws IOException {
		var reqName = getParameterValue("request");
		
		RequestContainer req = lookupRequest(reqName);
		
		ResponseContainer response = executeRequest(req);
		
		presentResponse(response, isOption("verbose"));
		
		return null;
	}


	protected RequestContainer lookupRequest(String reqName) {
		RequestContainer req = context.findRequestByIdish(reqName)
						.orElseThrow(()  -> new IllegalArgumentException("Request not found " + reqName));
		return req;
	}


	protected void presentResponse(ResponseContainer response, boolean verbose) {
		StringBuilder b = new StringBuilder();
		boolean isFirst = true;
		List<AspectCliPresenter> aspectPresenters = plugins.loadOrderedSpiInstances(AspectCliPresenter.class);
		for (AspectCliPresenter presenter : aspectPresenters) {
			for(ResponseAspect aspect : response.getAspects()) {
				if (presenter.canHandleAspect(aspect) && (verbose || presenter.isPrimary())) {
					if (!isFirst) {
						b.append(System.lineSeparator());
						b.append(System.lineSeparator());
					}
					if (verbose) {
						b.append(aspect.getName()).append(":").append(System.lineSeparator());
					}
					b.append(presenter.getStringRepresentation(aspect));
					
					isFirst = false;
				}
			}
		}
		
		try {
			if (isOption("less")) {
				InputStream inputStream = IOUtils.toInputStream(b.toString());
				Commands.less(TerminalUi.getTerminal(), inputStream, System.out, System.err, null, new String[] {"-"});
			} else {
				System.out.println(b.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private ResponseContainer executeRequest(RequestContainer req) {
		RequestTypePlugin requestTypePlugin = requestTypeManager.getPluginFor(req);
		return requestTypePlugin.executeRequest(req, buildTemplater());
	}
	private Templater buildTemplater() {
		Optional<Environment> activeEnv = context.getCurrentWorkspace().getEnvironments().stream().filter(e -> e.isActive()).findAny();
		List<Environment> globalEnvs = context.getCurrentWorkspace().getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList());
		return new EnvironmentTemplater(activeEnv, globalEnvs, new PrefixedTemplaterResolver(plugins.loadTemplaterPlugins()));
	}


	@Override
	public String getName() {
		return "execute-request";
	}

	@Override
	public String getAlias() {
		return "req";
	}

	@Override
	public String getDescription() {
		return "Executes a given request";
	}


	@Override
	public List<Parameter> createParameters() {
		return List.of(new Parameter("request", "the name of the request to execute", new Completion() {
			@Override
			public Collection<String> getCompletionCandidates() {
				return context.getAvailableRequestNames();
			}
		}));
	}

	@Override
	protected List<Option> createOptions() {
		return List.of(
				new Option("less", "l", "outputs response into less"),
				new Option("verbose", "v", "outputs all aspects")
		);
	}

}