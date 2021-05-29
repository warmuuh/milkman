package milkman.plugin.ws;

import milkman.utils.AsyncResponseControl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reactivestreams.Subscriber;

import java.net.URI;

public class MilkmanWebsocketClient extends WebSocketClient {

	private final String message;
	private final Subscriber<byte[]> responseSubscriber;
	private final AsyncResponseControl.AsyncControl asyncControl;


	public MilkmanWebsocketClient(URI serverUri,
								  String message,
								  Subscriber<byte[]> responseSubscriber,
								  AsyncResponseControl.AsyncControl asyncControl) {
		super(serverUri);
		this.message = message;
		this.responseSubscriber = responseSubscriber;
		this.asyncControl = asyncControl;
		asyncControl.onCancellationRequested.add(() -> close());
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		send(message);
	}

	@Override
	public void onMessage(String message) {
		responseSubscriber.onNext(message.getBytes());
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		responseSubscriber.onComplete();
		asyncControl.triggerRequestSucceeded();
	}

	@Override
	public void onError(Exception ex) {
		responseSubscriber.onError(ex);
		asyncControl.triggerRequestFailed(ex);
	}
}
