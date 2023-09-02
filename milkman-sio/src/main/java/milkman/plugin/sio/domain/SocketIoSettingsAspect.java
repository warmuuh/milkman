package milkman.plugin.sio.domain;

import static milkman.plugin.sio.domain.SocketIoVersion.SOCKETIO_V4;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class SocketIoSettingsAspect extends RequestAspect {

    public SocketIoSettingsAspect() {
        super("settings");
    }

    SocketIoVersion clientVersion = SOCKETIO_V4;
    String handshakePath = "/socket.io";
}
