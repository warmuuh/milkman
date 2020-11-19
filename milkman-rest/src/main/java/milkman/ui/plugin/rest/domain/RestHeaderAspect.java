package milkman.ui.plugin.rest.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

import java.util.LinkedList;
import java.util.List;

@Data
public class RestHeaderAspect  extends RequestAspect {

	private List<HeaderEntry> entries = new LinkedList<>();
	
	public RestHeaderAspect() {
		super("headers");
	}
	
}
