package milkman.plugin.mcp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class McpPromptsAspect extends RequestAspect {

	String selectedPrompt;
	String query;

	@JsonIgnore
	List<McpSchema.Prompt> prompts = List.of();

	@JsonIgnore
	public Optional<McpSchema.Prompt> getSelectedMcpPrompt() {
		if (selectedPrompt == null || selectedPrompt.isEmpty())
			return Optional.empty();
		for (McpSchema.Prompt prompt : prompts) {
			if (prompt.name().equals(selectedPrompt))
				return Optional.of(prompt);
		}
		return Optional.empty();
	}

	public McpPromptsAspect() {
		super("prompts");
	}
}
