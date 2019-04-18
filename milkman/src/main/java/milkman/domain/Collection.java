package milkman.domain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Collection implements Searchable {
	private String id;
	@ToString.Include private String name;
	private boolean starred;
	private List<RequestContainer> requests;
	
	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(name, searchString)
				|| requests.stream().anyMatch(r -> r.match(searchString));
	}
}
