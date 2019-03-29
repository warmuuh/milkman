package milkman.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
public abstract class RequestContainer {

	private String id = "";
	private String name;
	private List<RequestAspect> aspects = new LinkedList<RequestAspect>();
	
	public RequestContainer(String name) {
		super();
		this.name = name;
	}
	
}
