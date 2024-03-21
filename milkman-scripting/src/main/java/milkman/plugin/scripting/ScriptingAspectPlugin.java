package milkman.plugin.scripting;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.scripting.nashorn.NashornExecutor;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ScriptingAspectPlugin implements RequestAspectsPlugin, ToasterAware, LifecycleAware {

	private Toaster toaster;
	private static ScriptExecutor executor;

	public static ScriptExecutor getGlobalExecutor(){
		return executor;
	}

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
	@SneakyThrows
	public void beforeRequestExecution(RequestContainer request, RequestExecutionContext context) {
		var a = request.getAspect(ScriptingAspect.class).orElseThrow(() -> new IllegalArgumentException("script aspect not found"));
		var result = executeScript(a.getPreRequestScript(), request, null, context);
		a.setPreScriptOutput(result.getConsoleOutput());
		if (result.getError().isPresent()){
			throw result.getError().get();
		}
	}


	@Override
	@SneakyThrows
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		var a = request.getAspect(ScriptingAspect.class).orElseThrow(() -> new IllegalArgumentException("script aspect not found"));
		var result = executeScript(a.getPostRequestScript(), request, response, context);
		response.getAspects().add(new ScriptingOutputAspect(a.getPreScriptOutput(), result.getConsoleOutput()));
		if (result.getError().isPresent()){
			throw result.getError().get();
		}
	}


	@Override
	public int getOrder() {
		return 40;
	}

	@Override
	public void setToaster(Toaster toaster) {
		this.toaster = toaster;
	}


	private ScriptExecutor.ExecutionResult executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		try{
			if (StringUtils.isNotBlank(source)){
				ScriptExecutor.ExecutionResult executionResult = executor.executeScript(source, request, response, context);
				return executionResult;
			}
			return new ScriptExecutor.ExecutionResult("", Optional.empty(), Optional.empty());
		} catch (Throwable t){
			toaster.showToast("Failed to execute script: " + t.getMessage());
			throw t;
		}
	}

	@Override
	public void onPostConstruct() {
		new Thread(){
			@Override
			public void run() {
				executor = new NashornExecutor(toaster);
				//already load scripts
				((NashornExecutor)executor).initGlobalBindings();
			}
		}.start();
	}
}
