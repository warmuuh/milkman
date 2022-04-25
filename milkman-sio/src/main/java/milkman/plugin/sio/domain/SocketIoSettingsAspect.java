package milkman.plugin.sio.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

import static milkman.plugin.sio.domain.SocketIoVersion.SOCKETIO_V4;

@Data
public class SocketIoSettingsAspect extends RequestAspect {

    public SocketIoSettingsAspect() {
        super("settings");
    }

    SocketIoVersion clientVersion = SOCKETIO_V4;
    String handshakePath = "/socket.io";
}
