package milkman.ui.plugin.rest.domain;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
public class DebugRequestHeaderAspect implements ResponseAspect {

	private List<HeaderEntry> entries = new LinkedList<>();

	@Override
	public String getName() {
		return "debugHeaders";
	}
}
