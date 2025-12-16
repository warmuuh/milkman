package milkman.plugin.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.plugin.mcp.domain.McpResponseContainer;
import milkman.plugin.mcp.domain.McpTransportType;
import milkman.ui.plugin.Templater;

public class McpRequestProcessor {

  public McpResponseContainer connect(McpRequestContainer request, Templater templater) {

    if (request.getTransport() != McpTransportType.Sse) {
      throw new UnsupportedOperationException("Only SSE transport is supported for now");
    }

    // TODO: disconnect when already connected

    var transport = HttpClientSseClientTransport.builder(request.getUrl()).build();
    McpSyncClient client = McpClient.sync(transport).build();
    client.initialize();

    request.setMcpClient(client);

    McpResponseContainer responseContainer = new McpResponseContainer();
    responseContainer.setMcpClient(client);

    McpResponseAspect response = new McpResponseAspect();
    response.setResponse("mcp.result");
    responseContainer.setAspects(List.of(response));

    return responseContainer;
  }
}
