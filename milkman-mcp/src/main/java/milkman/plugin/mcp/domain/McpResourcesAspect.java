package milkman.plugin.mcp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class McpResourcesAspect extends RequestAspect {

	String selectedResource;
	String query;

	@JsonIgnore
	List<McpSchema.Resource> resources = List.of();

	@JsonIgnore
	public Optional<McpSchema.Resource> getSelectedMcpResource() {
		if (selectedResource == null || selectedResource.isEmpty())
			return Optional.empty();
		for (McpSchema.Resource resource : resources) {
			if (resource.name().equals(selectedResource))
				return Optional.of(resource);
		}
		return Optional.empty();
	}

	public McpResourcesAspect() {
		super("resources");
	}
}
