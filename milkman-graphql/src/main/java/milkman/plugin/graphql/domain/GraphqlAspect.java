package milkman.plugin.graphql.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class GraphqlAspect extends RequestAspect {
	private String query = "";
	private String variables = "{}";
	
	public GraphqlAspect() {
		super("graphql");
	}
	
}
