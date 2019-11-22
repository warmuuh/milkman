package milkman.plugin.grpc;

import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;

public class GrpcPlugin implements RequestTypePlugin, RequestAspectsPlugin {

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return null;
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return null;
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response,
			RequestExecutionContext context) {
		
	}

	@Override
	public RequestContainer createNewRequest() {
		return null;
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return null;
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		return null;
	}

	@Override
	public String getRequestType() {
		return null;
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return false;
	}

}
