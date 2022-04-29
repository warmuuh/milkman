package milkman.plugin.sio;

import milkman.domain.ResponseContainer;
import milkman.plugin.sio.domain.SocketIORequestContainer;
import milkman.plugin.sio.domain.SocketIOResponseAspect;
import milkman.plugin.sio.domain.SocketIoSettingsAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.utils.AsyncResponseControl.AsyncControl;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class SocketIOProcessor {


	public ResponseContainer executeRequest(SocketIORequestContainer request, Templater templater, AsyncControl asyncControl) {
		var settings = request.getAspect(SocketIoSettingsAspect.class).get();
		var url = templater.replaceTags(request.getUrl());

		var headers = request.getAspect(RestHeaderAspect.class).get().getEntries().stream()
			.filter(HeaderEntry::isEnabled)
			.collect(Collectors.toMap(h -> templater.replaceTags(h.getName()), h -> List.of(templater.replaceTags(h.getValue()))));

		var emitter = ReplayProcessor.<byte[]>create();

		var client = new MilkmanSocketIOClient(URI.create(url),
				settings.getHandshakePath(),
				settings.getClientVersion(),
				headers,
				emitter,
				asyncControl);

		asyncControl.triggerReqeuestStarted();
		client.connect();

		var response = new RestResponseContainer(url);
		response.getAspects().add(new RestResponseBodyAspect(emitter));
		response.getAspects().add(new SocketIOResponseAspect(client));
		return response;
	}
}
