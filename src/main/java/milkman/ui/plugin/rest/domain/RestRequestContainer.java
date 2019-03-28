package milkman.ui.plugin.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import milkman.domain.RequestContainer;

@Data
@EqualsAndHashCode(callSuper = true)
public class RestRequestContainer extends RequestContainer {

	private String url;
	private String httpMethod;
	
	
	public RestRequestContainer(String name, String url, String httpMethod) {
		super(name);
		this.url = url;
		this.httpMethod = httpMethod;
	}

	
}
