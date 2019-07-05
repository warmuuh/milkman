package milkman.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Folder {
	private String id;
	private @ToString.Include String name;
	private List<Folder> folders = new LinkedList<>();
	private List<String> requests = new LinkedList<>();
}
