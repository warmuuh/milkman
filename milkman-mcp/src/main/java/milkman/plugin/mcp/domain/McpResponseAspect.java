package milkman.plugin.mcp.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
public class McpResponseAspect implements ResponseAspect {

	String response;

	
	@Override
	public String getName() {
		return "result";
	}
}
