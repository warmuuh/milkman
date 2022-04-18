package milkman.plugin.ws.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class WebsocketAspect extends RequestAspect {
	private String message = "";

	public WebsocketAspect() {
		super("ws");
	}
	
}
