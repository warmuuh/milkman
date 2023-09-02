package milkman.plugin.sio;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SocketIoClientBuilder {
    SocketIoClientBuilder serverUri(URI serverUri);

    SocketIoClientBuilder headers(Map<String, List<String>> headers);

    SocketIoClientBuilder handshakePath(String handshakePath);

    SocketIoClientBuilder onConnect(Runnable onConnect);

    SocketIoClientBuilder onDisconnect(Runnable onDisconnect);

    SocketIoClientBuilder onConnectError(Consumer<String> onConnectError);

    SocketIoClientBuilder onEventMessage(BiConsumer<String, String> onEventMessage);

    SocketIoClient build();
}
