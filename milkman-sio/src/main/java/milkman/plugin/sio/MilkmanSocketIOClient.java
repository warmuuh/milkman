package milkman.plugin.sio;

import milkman.plugin.sio.socketio.IO;
import milkman.plugin.sio.socketio.Socket;
import milkman.utils.AsyncResponseControl.AsyncControl;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;

import org.reactivestreams.Subscriber;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MilkmanSocketIOClient {

	private final Subscriber<byte[]> responseSubscriber;
	private final AsyncControl asyncControl;
	private Socket socket;

	public MilkmanSocketIOClient(URI serverUri,
									String path,
									Map<String, List<String>> headers,
								  Subscriber<byte[]> responseSubscriber,
								  AsyncControl asyncControl) {
		if(path.length()==0) {
			path = null;
		}
		IO.Options options = IO.Options.builder()
			.setReconnection(false)
			.setPath(path)
			.setExtraHeaders(headers)
			.build();

		//a client that has an executor with 0 keepAliveTime
		OkHttpClient client = new OkHttpClient().newBuilder()
			.dispatcher(new Dispatcher(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false))))
			.build();
		IO.setDefaultOkHttpCallFactory(client);
		IO.setDefaultOkHttpWebSocketFactory(client);

		socket = IO.socket(serverUri, options);
		socket
			.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					asyncControl.triggerReqeuestStarted();
					asyncControl.triggerReqeuestReady();
				}
			})
			.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					responseSubscriber.onComplete();
					asyncControl.triggerRequestSucceeded();
				}
			})
			.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					Exception ex = new Exception(args[0].toString());
					responseSubscriber.onError(ex);
					asyncControl.triggerRequestFailed(ex);
				}
			})
			.on("*", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					outputMessage(args[0].toString(), args[1]);
				}
			});
		

		this.responseSubscriber = responseSubscriber;
		this.asyncControl = asyncControl;
		asyncControl.onCancellationRequested.add(() -> socket.disconnect());
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
			String msg = String.format("%s [%s]: \n%s\n\n", direction, event, message.toString());
			responseSubscriber.onNext(msg.getBytes());
		}
	}
	private void outputMessage(String event, Object message) {
		outputMessage("RECEIVED", event, message);
	}
}