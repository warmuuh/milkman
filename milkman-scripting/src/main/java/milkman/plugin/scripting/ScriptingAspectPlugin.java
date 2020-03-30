package milkman.plugin.scripting;

import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.scripting.graaljs.GraaljsExecutor;
import milkman.plugin.scripting.nashorn.NashornExecutor;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.ToasterAware;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ScriptingAspectPlugin implements RequestAspectsPlugin, ToasterAware {

	private Toaster toaster;
	private ScriptExecutor executor;
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new ScriptingAspectEditor());
	}


	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.singletonList(new ScriptingOutputEditor());
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (!request.getAspect(ScriptingAspect.class).isPresent())
			request.addAspect(new ScriptingAspect());
	}

	@Override
	public void beforeRequestExecution(RequestContainer request, RequestExecutionContext context) {
		request.getAspect(ScriptingAspect.class).ifPresent(a -> {
			var log = executeScript(a.getPreRequestScript(), request, null, context);
			a.setPreScriptOutput(log);
		});
	}


	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		request.getAspect(ScriptingAspect.class).ifPresent(a -> {
			var log = executeScript(a.getPostRequestScript(), request, response, context);
			response.getAspects().add(new ScriptingOutputAspect(a.getPreScriptOutput(), log));
		});
	}


	@Override
	public int getOrder() {
		return 40;
	}

	@Override
	public void setToaster(Toaster toaster) {
		this.toaster = toaster;
	}


	private String executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		try{
			if (executor == null){
				executor = new GraaljsExecutor(toaster);
			}
			return  executor.executeScript(source, request, response, context);
		} catch (Throwable t){
			t.printStackTrace();
			toaster.showToast("Failed to execute script: " + t.getMessage());
		}
		return "";
	}

}
