package milkman.plugin.scripting;

import java.util.Collections;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;

public class ScriptingAspectPlugin implements RequestAspectsPlugin {

	static ScriptEngine engine = initScriptEngine();
	
	
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
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		request.getAspect(ScriptingAspect.class).ifPresent(a -> {
			executeScript(a.getPostRequestScript(), request, response, context);
		});
	}

	private void executeScript(String postRequestScript, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		Bindings bindings = engine.createBindings();
		bindings.put("milkman", new MilkmanScriptingFacade(response, context));
		try {
			Object eval = engine.eval(postRequestScript, bindings);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

}
