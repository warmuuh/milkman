package milkman.plugin.sio;

import milkman.plugin.sio.domain.SocketIoVersion;
import milkman.utils.AsyncResponseControl.AsyncControl;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class MilkmanSocketIOClient {

	private final Subscriber<byte[]> responseSubscriber;
	private final AsyncControl asyncControl;
	private final SocketIoClient client;

	public MilkmanSocketIOClient(URI serverUri,
									String handshakePath,
									SocketIoVersion version,
									Map<String, List<String>> headers,
								  Subscriber<byte[]> responseSubscriber,
								  AsyncControl asyncControl) {

		client = getSocketIoClientBuilder(version)
				.serverUri(serverUri)
				.handshakePath(handshakePath)
				.headers(headers)
				.onConnect(() -> {
					asyncControl.triggerReqeuestStarted();
					asyncControl.triggerReqeuestReady();
				})
				.onDisconnect(() -> {
					responseSubscriber.onComplete();
					asyncControl.triggerRequestSucceeded();
				})
				.onConnectError(err -> {
					Exception ex = new Exception(err);
					responseSubscriber.onError(ex);
					asyncControl.triggerRequestFailed(ex);
				})
				.onEventMessage((evt, msg) -> outputMessage(evt, msg))
				.build();

		this.responseSubscriber = responseSubscriber;
		this.asyncControl = asyncControl;
		asyncControl.onCancellationRequested.add(() -> client.disconnect());
	}

	private SocketIoClientBuilder getSocketIoClientBuilder(SocketIoVersion version) {
		// based on compatibility matrix:
		// https://github.com/socketio/socket.io-client-java#compatibility
		switch (version) {
			case SOCKETIO_V1:
				return SocketIoV09Client.builder();
			case SOCKETIO_V2:
				return SocketIoV1Client.builder();
			case SOCKETIO_V3:
			case SOCKETIO_V4:
				return SocketIoV2Client.builder();
			default:
				throw new IllegalArgumentException("Unknown socket.io version");
		}
	}

	public void emit(String event, String message) {
		outputMessage("SENT", event, message);
		client.emit(event, message, response -> outputMessage(event, response));
	}

	public void connect() {
		client.connect();
	}

	public boolean connected() {
		return client.isConnected();
	}

	private void outputMessage(String direction, String event, Object message) {
		if(message != null) {
			String msg = String.format("%s [%s]: \n%s\n\n", direction, event, message);
			responseSubscriber.onNext(msg.getBytes());
		}
	}
	private void outputMessage(String event, Object message) {
		outputMessage("RECEIVED", event, message);
	}
}
