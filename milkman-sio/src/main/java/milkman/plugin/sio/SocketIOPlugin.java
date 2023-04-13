package milkman.plugin.sio;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.sio.domain.SocketIOAspect;
import milkman.plugin.sio.domain.SocketIORequestContainer;
import milkman.plugin.sio.domain.SocketIoSettingsAspect;
import milkman.plugin.sio.editor.SocketIOAspectEditor;
import milkman.plugin.sio.editor.SocketIORequestEditor;
import milkman.plugin.sio.editor.SocketIoSettingsAspectEditor;
import milkman.ui.plugin.*;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.AsyncResponseControl.AsyncControl;

import java.util.Collections;
import java.util.List;

public class SocketIOPlugin implements RequestAspectsPlugin, RequestTypePlugin {

	SocketIOProcessor requestProcessor = new SocketIOProcessor();
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return List.of(new SocketIOAspectEditor(), new SocketIoSettingsAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof SocketIORequestContainer) {
			if (!request.getAspect(SocketIOAspect.class).isPresent()) {
				request.addAspect(new SocketIOAspect());
			}

			if (!request.getAspect(SocketIoSettingsAspect.class).isPresent()) {
				request.addAspect(new SocketIoSettingsAspect());
			}

			if (!request.getAspect(RestHeaderAspect.class).isPresent()) {
				var headers = new RestHeaderAspect();
				request.addAspect(headers);
			}

		}
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		

	}

	@Override
	public int getOrder() {
		return 13;
	}

	@Override
	public RequestContainer createNewRequest() {
		return new SocketIORequestContainer("New Socket.IO Request", "");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new SocketIORequestEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater,
			AsyncControl asyncControl) {
		return requestProcessor.executeRequest((SocketIORequestContainer) request, templater, asyncControl);

	}
	
	@Override
	public String getRequestType() {
		return "Socket.IO";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof SocketIORequestContainer;
	}

	
}
