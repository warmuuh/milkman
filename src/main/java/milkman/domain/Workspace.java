package milkman.domain;

import java.util.List;

import lombok.Data;

@Data
public class Workspace {

	private final List<Collection> collections;
}
