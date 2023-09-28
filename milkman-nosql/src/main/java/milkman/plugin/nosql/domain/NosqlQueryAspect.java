package milkman.plugin.nosql.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class NosqlQueryAspect extends RequestAspect {

	String query;
	
	public NosqlQueryAspect() {
		super("query");
	}
}
