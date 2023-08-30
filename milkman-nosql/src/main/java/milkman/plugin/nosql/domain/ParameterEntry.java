package milkman.plugin.nosql.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParameterEntry {
	
	@EqualsAndHashCode.Include private String id = UUID.randomUUID().toString();
	private String name;
	private String value;
	private boolean enabled;
//	@Override
//	public boolean equals(Object obj) {
//		return obj == this;
//	}
//	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + (enabled ? 1231 : 1237);
//		result = prime * result + ((name == null) ? 0 : name.hashCode());
//		result = prime * result + ((value == null) ? 0 : value.hashCode());
//		return result;
//	}
	
}