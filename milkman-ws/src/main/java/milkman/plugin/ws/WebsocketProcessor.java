package milkman.plugin.ws;

import milkman.domain.ResponseContainer;
import milkman.plugin.ws.domain.WebsocketAspect;
import milkman.plugin.ws.domain.WebsocketRequestContainer;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.utils.AsyncResponseControl;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;

public class WebsocketProcessor {


	public ResponseContainer executeRequest(WebsocketRequestContainer request, Templater templater, AsyncResponseControl.AsyncControl asyncControl) {
		var url = templater.replaceTags(request.getUrl());

		var wsAspect = request.getAspect(WebsocketAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Missing Websocket aspect"));

		var emitter = ReplayProcessor.<byte[]>create();

		var client = new MilkmanWebsocketClient(URI.create(url), wsAspect.getMessage(), emitter, asyncControl);

		asyncControl.triggerReqeuestStarted();
		client.connect();

		var responseBody = new RestResponseBodyAspect(emitter);
		var response = new RestResponseContainer(url);
		response.getAspects().add(responseBody);
		return response;
	}
}
