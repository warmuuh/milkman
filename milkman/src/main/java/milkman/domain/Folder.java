package milkman.domain;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.reactfx.util.Lists;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Folder implements Searchable {
	private String id;
	private @ToString.Include String name;
	private List<Folder> folders = new LinkedList<>();
	private List<String> requests = new LinkedList<>();
	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(name, searchString);
	}
	public List<String> getAllRequestsIncludingSubFolders() {
		var result = new LinkedList<String>();
		result.addAll(requests);
		folders.forEach(f -> result.addAll(f.getAllRequestsIncludingSubFolders()));
		return result;
	}
}
