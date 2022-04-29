package milkman.plugin.sio;

import java.util.function.Consumer;

public interface SocketIoClient {

    void disconnect();

    void connect();

    boolean isConnected();

    void emit(String event, String message, Consumer<String> responseHandler);
}
