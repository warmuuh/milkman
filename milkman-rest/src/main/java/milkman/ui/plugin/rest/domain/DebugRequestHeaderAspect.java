package milkman.ui.plugin.rest.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;

import java.util.LinkedList;
import java.util.List;

@Data
public class DebugRequestHeaderAspect implements ResponseAspect {

	private List<HeaderEntry> entries = new LinkedList<>();

	@Override
	public String getName() {
		return "debugHeaders";
	}
}
