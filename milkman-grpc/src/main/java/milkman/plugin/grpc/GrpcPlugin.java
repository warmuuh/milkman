package milkman.plugin.grpc;

import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.editor.GrpcOperationAspectEditor;
import milkman.plugin.grpc.editor.GrpcRequestEditor;
import milkman.plugin.grpc.editor.GrpcResponsePayloadEditor;
import milkman.plugin.grpc.processor.GrpcRequestProcessor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;

public class GrpcPlugin implements RequestTypePlugin, RequestAspectsPlugin {

	private GrpcRequestProcessor processor = new GrpcRequestProcessor();
	
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return List.of(new GrpcOperationAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return List.of(new GrpcResponsePayloadEditor());
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof GrpcRequestContainer) {
			if (request.getAspect(GrpcOperationAspect.class).isEmpty()) {
				request.addAspect(new GrpcOperationAspect());
			}
		}
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response,
			RequestExecutionContext context) {
		// nothing to do, we created the request
	}

	@Override
	public RequestContainer createNewRequest() {
		return new GrpcRequestContainer("New Grpc Request", "");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new GrpcRequestEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		if (!(request instanceof GrpcRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request type");
		}

		return processor.executeRequest((GrpcRequestContainer) request, templater);
	}

	@Override
	public String getRequestType() {
		return "Grpc";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof GrpcRequestContainer;
	}
	
	@Override
	public int getOrder() {
		return 18;
	}

}
