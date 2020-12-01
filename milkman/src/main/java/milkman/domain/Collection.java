package milkman.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.ToString.Include;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Collection extends Dirtyable implements Searchable {
	private String id;
	@Include private String name;
	private boolean starred;
	private List<RequestContainer> requests = new LinkedList<>();
	private List<Folder> folders = new LinkedList<>(); //initialize bc of deserialization
	
	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(name, searchString);
	}
	
}
