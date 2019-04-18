package milkman.ui.plugin.rest.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QueryParamEntry {
	@EqualsAndHashCode.Include private String id = UUID.randomUUID().toString();
	private String name;
	private String value;
}