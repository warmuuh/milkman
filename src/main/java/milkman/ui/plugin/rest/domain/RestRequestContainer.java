package milkman.ui.plugin.rest.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseAspect;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
