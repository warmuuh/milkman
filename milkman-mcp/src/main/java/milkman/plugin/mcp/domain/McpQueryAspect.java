package milkman.plugin.mcp.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class McpQueryAspect extends RequestAspect {

	String query;

	public McpQueryAspect() {
		super("query");
	}
}
