package milkman.plugin.nosql.domain;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class NosqlParameterAspect  extends RequestAspect {

	private List<ParameterEntry> entries = new LinkedList<>();
	
	public NosqlParameterAspect() {
		super("parameters");
	}
	
}
