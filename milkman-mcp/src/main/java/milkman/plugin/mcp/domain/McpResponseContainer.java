package milkman.plugin.mcp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.client.McpAsyncClient;
import lombok.Data;
import milkman.domain.ResponseContainer;

@Data
public class McpResponseContainer extends ResponseContainer {

  @JsonIgnore
  McpAsyncClient mcpClient;

}
