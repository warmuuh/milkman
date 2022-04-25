package milkman.plugin.sio;

import io.socket.client.Ack;
import milkman.plugin.sio.socketio.IO;
import milkman.plugin.sio.socketio.IO.Options;
import milkman.plugin.sio.socketio.Socket;
import milkman.plugin.sio.socketio.SocketOptionBuilder;
import milkman.utils.AsyncResponseControl.AsyncControl;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.reactivestreams.Subscriber;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MilkmanSocketIOClient {

	private final Subscriber<byte[]> responseSubscriber;
	private final AsyncControl asyncControl;
	private final Socket socket;

	public MilkmanSocketIOClient(URI serverUri,
									String handshakePath,
									Map<String, List<String>> headers,
								  Subscriber<byte[]> responseSubscriber,
								  AsyncControl asyncControl) {

		SocketOptionBuilder optionsBuilder = Options.builder()
				.setReconnection(false)
				.setExtraHeaders(headers);

		if(handshakePath.length() > 0) {
			optionsBuilder.setPath(handshakePath);
		}

		initializeOkHttpClientForSocketIo();

		socket = IO.socket(serverUri, optionsBuilder.build());
		socket
			.on(Socket.EVENT_CONNECT, args -> {
				asyncControl.triggerReqeuestStarted();
				asyncControl.triggerReqeuestReady();
			})
			.on(Socket.EVENT_DISCONNECT, args -> {
				responseSubscriber.onComplete();
				asyncControl.triggerRequestSucceeded();
			})
			.on(Socket.EVENT_CONNECT_ERROR, args -> {
				Exception ex = new Exception(args[0].toString());
				responseSubscriber.onError(ex);
				asyncControl.triggerRequestFailed(ex);
			})
			.on("*", args -> outputMessage(args[0].toString(), args[1]));
		

		this.responseSubscriber = responseSubscriber;
		this.asyncControl = asyncControl;
		asyncControl.onCancellationRequested.add(() -> socket.disconnect());
	}

	private void initializeOkHttpClientForSocketIo() {
		//a client that has an executor with 0 keepAliveTime
		OkHttpClient client = new OkHttpClient().newBuilder()
			.dispatcher(new Dispatcher(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false))))
			.build();
		IO.setDefaultOkHttpCallFactory(client);
		IO.setDefaultOkHttpWebSocketFactory(client);
	}

	public void emit(String event, String message) {
		outputMessage("SENT", event, message);
		socket.emit(event, message, (Ack) args -> outputMessage(event, args[0]));
	}

	public void connect() {
		socket.connect();
	}

	public boolean connected() {
		return socket.connected();
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
