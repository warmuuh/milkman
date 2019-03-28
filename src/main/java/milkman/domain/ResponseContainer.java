package milkman.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class ResponseContainer {

	private final List<ResponseAspect> aspects = new LinkedList<ResponseAspect>();
	
}
