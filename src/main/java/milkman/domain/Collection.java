package milkman.domain;

import java.util.List;

import lombok.Data;

@Data
public class Collection {
	private final String name;
	private final List<RequestContainer> requests;
}
