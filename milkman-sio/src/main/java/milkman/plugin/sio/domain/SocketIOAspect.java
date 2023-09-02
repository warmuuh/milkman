package milkman.plugin.sio.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class SocketIOAspect extends RequestAspect {
	private String event = "";
	private String message = "";

	public SocketIOAspect() {
		super("sio");
	}
	
}
