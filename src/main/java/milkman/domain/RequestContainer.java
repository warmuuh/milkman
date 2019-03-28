package milkman.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public abstract class RequestContainer {

	private String id = UUID.randomUUID().toString();
	private final String name;
	private final List<RequestAspect> aspects = new LinkedList<RequestAspect>();
	
}
