package milkman.plugin.sio;

import milkman.domain.ResponseContainer;
import milkman.plugin.sio.domain.SocketIORequestContainer;
import milkman.plugin.sio.domain.SocketIOResponseAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.utils.AsyncResponseControl;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;

public class SocketIOProcessor {


	public ResponseContainer executeRequest(SocketIORequestContainer request, Templater templater, AsyncResponseControl.AsyncControl asyncControl) {
		var url = templater.replaceTags(request.getUrl());
		var path = templater.replaceTags(request.getPath());

		var emitter = ReplayProcessor.<byte[]>create();

		var client = new MilkmanSocketIOClient(URI.create(url), path, emitter, asyncControl);

		asyncControl.triggerReqeuestStarted();
		client.connect();

		var response = new RestResponseContainer(url);
		response.getAspects().add(new RestResponseBodyAspect(emitter));
		response.getAspects().add(new SocketIOResponseAspect(client));
		return response;
	}
}
