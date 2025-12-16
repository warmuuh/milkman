package milkman.plugin.mcp;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpQueryAspect;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.plugin.mcp.domain.McpResponseContainer;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.plugin.mcp.domain.McpTransportType;
import milkman.ui.plugin.Templater;
import milkman.utils.AsyncResponseControl;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class McpRequestProcessor {

  public McpResponseContainer connect(
      McpRequestContainer request,
      Templater templater,
      AsyncResponseControl.AsyncControl asyncControl
  ) {

    if (request.getTransport() != McpTransportType.Sse) {
      throw new UnsupportedOperationException("Only SSE transport is supported for now");
    }

    // TODO: disconnect when already connected

    var transport = HttpClientSseClientTransport.builder(request.getUrl()).build();
    McpAsyncClient client = McpClient.async(transport).build();
    client.initialize().subscribeOn(Schedulers.boundedElastic())
        .subscribe(res -> {
          asyncControl.triggerReqeuestStarted();
          asyncControl.triggerReqeuestReady();
        });

    McpResponseContainer responseContainer = new McpResponseContainer();
    responseContainer.setMcpClient(client);

    McpResponseAspect response = new McpResponseAspect();
    response.setResponse("trigger request in Query editor...");
    responseContainer.getAspects().add(response);

    asyncControl.onCancellationRequested.add(() -> {
      System.out.println("Disconnecting MCP client...");
      client.close();
      asyncControl.triggerRequestSucceeded();
    });

    return responseContainer;
  }

  public void executeRequest(RequestContainer request, ResponseContainer response) {

    McpToolsAspect toolAspect = request.getAspect(McpToolsAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing tools aspect"));

    McpQueryAspect queryAspect = request.getAspect(McpQueryAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing query aspect"));
    McpSchema.Tool selectedTool = toolAspect.getSelectedMcpTool()
        .orElseThrow(() -> new IllegalArgumentException("No tool selected"));

    McpResponseAspect responseAspect = response.getAspect(McpResponseAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing response aspect"));

    McpAsyncClient mcpClient = ((McpResponseContainer) response).getMcpClient();
    mcpClient.callTool(McpSchema.CallToolRequest.builder()
            .name(selectedTool.name())
            .arguments(queryAspect.getQuery())
        .build())
        .subscribe(
            result -> updateResponse(result, responseAspect),
            error -> responseAspect.setResponse("Failed to call tool: " + error.getMessage()));
  }

  private static void updateResponse(McpSchema.CallToolResult result, McpResponseAspect responseAspect) {
    StringBuilder sb = new StringBuilder();
    for (McpSchema.Content content : result.content()) {
      sb.append("Content Type: ").append(content.type()).append("\n");
      switch (content) {
        case McpSchema.TextContent text -> sb.append(text.text()).append("\n");
        case McpSchema.EmbeddedResource embedded -> {
          if (embedded.resource() instanceof McpSchema.TextResourceContents textResource) {
            sb.append(textResource.text()).append("\n");
          } else {
            sb.append("<non-text embedded resource>\n");
          }
        }
        default -> sb.append("<omitted>\n");
      }
    }

    responseAspect.setResponse(sb.toString());
  }
}
