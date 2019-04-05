package milkman.ui.plugin.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeaderEntry {
	private String name;
	private String value;
	private boolean enabled;
}