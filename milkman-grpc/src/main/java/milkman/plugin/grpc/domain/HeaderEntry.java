package milkman.plugin.grpc.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HeaderEntry {
	
	@EqualsAndHashCode.Include private String id = UUID.randomUUID().toString();
	private String name;
	private String value;
	private boolean enabled;
	
}