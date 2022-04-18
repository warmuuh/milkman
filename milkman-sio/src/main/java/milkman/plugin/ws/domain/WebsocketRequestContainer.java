package milkman.plugin.ws.domain;

import lombok.*;
import milkman.domain.RequestContainer;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebsocketRequestContainer extends RequestContainer {

	private String url; 
	
	@Override
	public String getType() {
		return "ws";
	}

	public WebsocketRequestContainer(String name, String url){
		super(name);
		this.url = url;
	}
	
}
