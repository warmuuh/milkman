package milkman.plugin.mcp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.Data;
import milkman.domain.ResponseContainer;

@Data
public class McpResponseContainer extends ResponseContainer {

  @JsonIgnore
  McpSyncClient mcpClient;

}
