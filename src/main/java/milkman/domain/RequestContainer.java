package milkman.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class RequestContainer {

	private final String name;
	private final List<RequestAspect> aspects = new LinkedList<RequestAspect>();
	
}
