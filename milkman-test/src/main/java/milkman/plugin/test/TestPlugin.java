package milkman.plugin.test;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.plugin.test.domain.TestContainer;
import milkman.plugin.test.editor.TestAspectEditor;
import milkman.plugin.test.editor.TestAspectScenarioEditor;
import milkman.plugin.test.editor.TestContainerEditor;
import milkman.plugin.test.editor.TestResponseAspectEditor;
import milkman.ui.plugin.*;
import milkman.utils.AsyncResponseControl;

import java.util.Collections;
import java.util.List;

public class TestPlugin implements RequestAspectsPlugin, RequestTypePlugin, RequestExecutorAware {

	private PluginRequestExecutor requestExecutor;

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return List.of(new TestAspectScenarioEditor(), new TestAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.singletonList(new TestResponseAspectEditor());
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {

	}

	@Override
	public int getOrder() {
		return 10;
	}


	@Override
	public RequestContainer createNewRequest() {
		var container = new TestContainer("New Test");
		container.addAspect(new TestAspect());
		return container;
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new TestContainerEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		throw new UnsupportedOperationException();
	}

	public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater, AsyncResponseControl.AsyncControl asyncControl) {
		return new TestRunner(requestExecutor).executeRequest((TestContainer)request, templater, asyncControl);
	};

	@Override
	public String getRequestType() {
		return "TEST";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof TestContainer;
	}

	@Override
	public void setRequestExecutor(PluginRequestExecutor executor) {
		requestExecutor = executor;
	}
}
