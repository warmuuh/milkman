package milkman.domain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collection implements Searchable {
	private String id;
	private String name;
	private List<RequestContainer> requests;
	
	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(name, searchString)
				|| requests.stream().anyMatch(r -> r.match(searchString));
	}
}
