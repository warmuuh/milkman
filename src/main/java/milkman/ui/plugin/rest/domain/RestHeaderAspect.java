package milkman.ui.plugin.rest.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.RequestAspect;

@Data
public class RestHeaderAspect extends RequestAspect {

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class HeaderEntry {
		private String name;
		private String value;
		private boolean enabled;
	}
	
	
	private List<HeaderEntry> entries = new LinkedList<>();
	
	public RestHeaderAspect() {
//		entries.add(new HeaderEntry("Accept", "application/json", true));
	}
}
