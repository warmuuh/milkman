package milkman.ui.plugin.rest.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class RestBodyAspect extends RequestAspect {

	String body;
	
	public RestBodyAspect() {
		super("body");
	}
}
