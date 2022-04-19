package milkman.plugin.sio.domain;

import lombok.*;
import milkman.domain.RequestContainer;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SocketIORequestContainer extends RequestContainer {

	private String url;
	private String path;
	
	@Override
	public String getType() {
		return "sio";
	}

	public SocketIORequestContainer(String name, String url, String path){
		super(name);
		this.url = url;
		this.path = path;
	}
	
}
