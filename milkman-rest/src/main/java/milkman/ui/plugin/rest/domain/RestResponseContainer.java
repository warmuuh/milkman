package milkman.ui.plugin.rest.domain;

import lombok.Data;
import milkman.domain.ResponseContainer;

@Data
public class RestResponseContainer extends ResponseContainer {

	private final String url;
	
}
