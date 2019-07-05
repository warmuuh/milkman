package milkman.domain;

import java.util.LinkedList;
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
public class Collection extends Dirtyable implements Searchable {
	private String id;
	@ToString.Include private String name;
	private boolean starred;
	private List<RequestContainer> requests;
	private List<Folder> folders = new LinkedList<Folder>(); //initialize bc of deserialization
	
	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(name, searchString);
	}
	
}
