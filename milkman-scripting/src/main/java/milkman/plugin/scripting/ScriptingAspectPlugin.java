package milkman.plugin.scripting;

import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.ToasterAware;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ScriptingAspectPlugin implements RequestAspectsPlugin, ToasterAware {

	static ScriptEngine engine = initScriptEngine();
	private Toaster toaster;
	
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new ScriptingAspectEditor());
	}

	private static ScriptEngine initScriptEngine() {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		return engine;
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (!request.getAspect(ScriptingAspect.class).isPresent())
			request.addAspect(new ScriptingAspect());
	}

	@Override
	public void beforeRequestExecution(RequestContainer request, RequestExecutionContext context) {
		request.getAspect(ScriptingAspect.class).ifPresent(a -> {
			executeScript(a.getPreRequestScript(), request, null, context);
		});
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		request.getAspect(ScriptingAspect.class).ifPresent(a -> {
			executeScript(a.getPostRequestScript(), request, response, context);
		});
	}

	private void executeScript(String postRequestScript, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		Bindings bindings = engine.createBindings();
		bindings.put("milkman", new MilkmanScriptingFacade(request, response, context, toaster));
		try {
			Object eval = engine.eval(postRequestScript, bindings);
		} catch (ScriptException e) {
			String causeMessage = ExceptionUtils.getRootCauseMessage(e);
			toaster.showToast("Failed to execute script: " + causeMessage);
			log.error("failed to execute script", e);
			
		}
	}

	@Override
	public int getOrder() {
		return 30;
	}

	@Override
	public void setToaster(Toaster toaster) {
		this.toaster = toaster;
	}
	
	

}
