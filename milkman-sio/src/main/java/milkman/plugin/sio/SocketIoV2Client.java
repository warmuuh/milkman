package milkman.plugin.sio;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import milkman.sio.shaded.v2.client.IO;
import milkman.sio.shaded.v2.client.IO.Options;
import milkman.sio.shaded.v2.client.Socket;
import milkman.sio.shaded.v2.client.SocketOptionBuilder;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketIoV2Client implements SocketIoClient {
    private final Socket socket;

    @Override
    public void disconnect() {
        socket.disconnect();
    }

    @Override
    public void connect() {
        socket.connect();
    }

    @Override
    public boolean isConnected() {
        return socket.connected();
    }

    @Override
    public void emit(String event, String message, Consumer<String> responseHandler) {
        socket.emit(event, new Object[]{message}, args -> responseHandler.accept(args[0].toString()));
    }

    @Builder
    static SocketIoV2Client create(
            URI serverUri,
            Map<String, List<String>> headers,
            String handshakePath,
            Runnable onConnect,
            Runnable onDisconnect,
            Consumer<String> onConnectError,
            BiConsumer<String, String> onEventMessage
    ) {
        SocketOptionBuilder optionsBuilder = Options.builder()
                .setReconnection(false)
                .setExtraHeaders(headers);

        if (handshakePath.length() > 0) {
            optionsBuilder.setPath(handshakePath);
        }

        initializeOkHttpClientForSocketIo();

        Socket socket = IO.socket(serverUri, optionsBuilder.build());
        socket
                .on(Socket.EVENT_CONNECT, args -> {
                    onConnect.run();
                })
                .on(Socket.EVENT_DISCONNECT, args -> {
                    onDisconnect.run();
                })
                .on(Socket.EVENT_CONNECT_ERROR, args -> {
                    onConnectError.accept(args[0].toString());
                })
                .on("*", args -> onEventMessage.accept(args[0].toString(), args[1].toString()));
        return new SocketIoV2Client(socket);
    }

    public static class SocketIoV2ClientBuilder implements SocketIoClientBuilder {

    }

    private static void initializeOkHttpClientForSocketIo() {
        //a client that has an executor with 0 keepAliveTime
        OkHttpClient client = new OkHttpClient().newBuilder()
                .dispatcher(new Dispatcher(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false))))
                .build();
        IO.setDefaultOkHttpCallFactory(client);
        IO.setDefaultOkHttpWebSocketFactory(client);
    }
}
