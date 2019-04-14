package milkman.ui.plugin.rest.domain;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import milkman.domain.RequestContainer;

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


	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(url, searchString)
				|| StringUtils.containsIgnoreCase(httpMethod, searchString)
				|| super.match(searchString);
	}


	@Override
	public String getType() {
		return this.httpMethod;
	}

}
