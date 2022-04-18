package milkman.plugin.ws;

import milkman.utils.AsyncResponseControl.AsyncControl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reactivestreams.Subscriber;

import java.net.URI;

public class MilkmanWebsocketClient extends WebSocketClient {

	private final Subscriber<byte[]> responseSubscriber;
	private final AsyncControl asyncControl;


	public MilkmanWebsocketClient(URI serverUri,
								  Subscriber<byte[]> responseSubscriber,
								  AsyncControl asyncControl) {
		super(serverUri);
		this.responseSubscriber = responseSubscriber;
		this.asyncControl = asyncControl;
		asyncControl.onCancellationRequested.add(() -> close());
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		asyncControl.triggerReqeuestReady();
	}

	@Override
	public void send(String message) {
		String msg = "SENT: \n" + message + "\n\n";
		responseSubscriber.onNext(msg.getBytes());
		super.send(message);
	}

	@Override
	public void onMessage(String message) {
		String msg = "RECEIVED: \n" + message + "\n\n";
		responseSubscriber.onNext(msg.getBytes());
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
