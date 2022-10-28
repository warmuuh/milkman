package milkman.ui.plugin.rest.domain;

import lombok.*;
import milkman.domain.RequestContainer;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@Getter @Setter
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
		return "HTTP";
	}

	@Override
	public RequestTypeDescriptor getTypeDescriptor() {
		return new RequestTypeDescriptor(httpMethod, styleFor(httpMethod));
	}

	private String styleFor(String httpMethod) {
		String style = "-fx-background-color: ";
		switch (httpMethod.toUpperCase(Locale.ROOT)) {
			case "POST":
				return style + "#eb7a34";
			case "GET":
				return style + "#34abeb";
			case "PUT":
				return style + "#257a35";
			case "DELETE":
				return style + "#eb3434";
			default:
				return null;
		}
	}
}
