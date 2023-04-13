package milkman.plugin.ws;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.ws.domain.WebsocketAspect;
import milkman.plugin.ws.domain.WebsocketRequestContainer;
import milkman.plugin.ws.editor.WebSocketAspectEditor;
import milkman.plugin.ws.editor.WebsocketRequestEditor;
import milkman.ui.plugin.*;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.AsyncResponseControl.AsyncControl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WebsocketPlugin implements RequestAspectsPlugin, RequestTypePlugin {

	WebsocketProcessor requestProcessor = new WebsocketProcessor();
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new WebSocketAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof WebsocketRequestContainer) {
			if (!request.getAspect(WebsocketAspect.class).isPresent())
				request.addAspect(new WebsocketAspect());
			
			if (!request.getAspect(RestHeaderAspect.class).isPresent()) {
				var headers = new RestHeaderAspect();
				headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), "Content-Type", "application/json", true));
				request.addAspect(headers);
			}

		}
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		

	}

	@Override
	public int getOrder() {
		return 12;
	}

	@Override
	public RequestContainer createNewRequest() {
		return new WebsocketRequestContainer("New Websocket Request", "");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new WebsocketRequestEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater,
			AsyncControl asyncControl) {
		return requestProcessor.executeRequest((WebsocketRequestContainer) request, templater, asyncControl);

	}
	
	@Override
	public String getRequestType() {
		return "WebSocket";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof WebsocketRequestContainer;
	}

	
}
