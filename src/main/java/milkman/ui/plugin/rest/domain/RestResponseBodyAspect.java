package milkman.ui.plugin.rest.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
public class RestResponseBodyAspect implements ResponseAspect {

	private final String body;
}
