package milkman.plugin.mcp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import milkman.domain.RequestAspect;
import milkman.utils.Event0;

@Data
public class McpToolsAspect extends RequestAspect {

	String selectedTool;

	@JsonIgnore
	List<McpSchema.Tool> tools = List.of();

	@JsonIgnore
	Event0 toolsChanged = new Event0();

	public void setSelectedTool(String selectedTool) {
		this.selectedTool = selectedTool;
		toolsChanged.invoke();
	}

	@JsonIgnore
	public Optional<McpSchema.Tool> getSelectedMcpTool() {
		if (selectedTool == null || selectedTool.isEmpty())
			return Optional.empty();
		for (McpSchema.Tool tool : tools) {
			if (tool.name().equals(selectedTool))
				return Optional.of(tool);
		}
		return Optional.empty();
	}

	public McpToolsAspect() {
		super("tools");
	}
}
