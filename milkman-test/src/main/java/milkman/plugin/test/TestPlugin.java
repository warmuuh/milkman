package milkman.plugin.test;

import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.plugin.test.domain.TestContainer;
import milkman.plugin.test.editor.TestAspectEditor;
import milkman.plugin.test.editor.TestContainerEditor;
import milkman.ui.plugin.*;

public class TestPlugin implements RequestAspectsPlugin, RequestTypePlugin {

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new TestAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (!request.getAspect(TestAspect.class).isPresent())
			request.addAspect(new TestAspect());
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {

	}

	@Override
	public int getOrder() {
		return 110;
	}


	@Override
	public RequestContainer createNewRequest() {
		return new TestContainer();
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new TestContainerEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		return null;
	}

	@Override
	public String getRequestType() {
		return "TEST";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof TestContainer;
	}
}
