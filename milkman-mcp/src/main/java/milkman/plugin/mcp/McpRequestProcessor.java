package milkman.plugin.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpInstructionsAspect;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.plugin.mcp.domain.McpResponseContainer;
import milkman.plugin.mcp.domain.McpStructuredOutputAspect;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.AsyncResponseControl;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class McpRequestProcessor {

  public McpResponseContainer connect(
      McpRequestContainer request,
      Templater templater,
      AsyncResponseControl.AsyncControl asyncControl
  ) {

    RestHeaderAspect headers = request.getAspect(RestHeaderAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing headers aspect"));
    Consumer<HttpRequest.Builder> addHeaders = httpRequest -> {
        for (var header : headers.getEntries()) {
          if (header.isEnabled()) {
            String headerName = templater.replaceTags(header.getName());
            String headerValue = templater.replaceTags(header.getValue());
            httpRequest.header(headerName, headerValue);
          }
        }
    };

    // TODO: support other transports
    // TODO: disconnect when already connected
    // TODO: figure out endpoint and set it explicitly
    var transport = switch (request.getTransport()) {
      case Sse -> HttpClientSseClientTransport
          .builder(request.getUrl())
          .customizeRequest(addHeaders)
          .build();
      case StreamableHttp -> HttpClientStreamableHttpTransport
          .builder(request.getUrl())
          .customizeRequest(addHeaders)
          .build();
      default ->  throw new UnsupportedOperationException("Only SSE transport is supported for now");
    };


    McpAsyncClient client = McpClient.async(transport)
        // TODO: setup consumers and show/trigger changes in UI
        .loggingConsumer(notice -> {
          log.info("MCP Notice: {}", notice);
          return Mono.empty();
        })
        .initializationTimeout(Duration.ofSeconds(5))
        .build();

    McpResponseContainer responseContainer = new McpResponseContainer();
    responseContainer.setMcpClient(client);


    McpInstructionsAspect instructionsAspect = new McpInstructionsAspect();
    responseContainer.getAspects().add(instructionsAspect);

    McpResponseAspect response = new McpResponseAspect();
    response.setResponse("trigger request in Query editor...");
    responseContainer.getAspects().add(response);

    McpStructuredOutputAspect structuredOutputAspect = new McpStructuredOutputAspect();
    structuredOutputAspect.setStructuredOutput("trigger request in Query editor...");
    responseContainer.getAspects().add(structuredOutputAspect);

    asyncControl.onCancellationRequested.add(() -> {
      client.close();
      asyncControl.triggerRequestSucceeded();
    });

    client.initialize().subscribeOn(Schedulers.boundedElastic())
        .subscribe(res -> {
          updateStatusOnInitialization(res, responseContainer);
          instructionsAspect.setInstructions(res.instructions());
          asyncControl.triggerReqeuestStarted();
          asyncControl.triggerReqeuestReady();
        }, err -> {
          updateErrorResponse(err, responseContainer);
          asyncControl.triggerRequestFailed(err);
        });

    return responseContainer;
  }

  private static void updateStatusOnInitialization(
      McpSchema.InitializeResult res, McpResponseContainer responseContainer) {
    responseContainer.getStatusInformations().add("Protocol", res.protocolVersion());
    responseContainer.getStatusInformations().add("Server", Map.of(
        "Server", res.serverInfo().name(),
        "Server Version", res.serverInfo().version()
    ));
    responseContainer.getStatusInformations().add("Capabilities", Map.of(
        "Tools", toCapabilityString(res.capabilities().tools()),
        "Completions", toCapabilityString(res.capabilities().completions()),
        "Logging", toCapabilityString(res.capabilities().logging()),
        "Prompts", toCapabilityString(res.capabilities().prompts()),
        "Resources", toCapabilityString(res.capabilities().resources()),
        "Experimental", res.capabilities().experimental() != null
            ? res.capabilities().experimental().keySet().toString()
            : "no"
    ));
  }

  private static String toCapabilityString(Object capabilityObject) {
    if (capabilityObject == null) {
      return "no";
    }
    ToStringStyle style = new ToStringStyle() {
      {
        this.setUseClassName(false);
        this.setUseIdentityHashCode(false);
        this.setContentStart("");
        this.setContentEnd("");
        this.setFieldSeparator(",");
        this.setFieldNameValueSeparator(":");
      }
    };
    String details = ToStringBuilder.reflectionToString(capabilityObject, style);
    if (details.isEmpty()) {
      return "yes";
    }
    return "yes (" + details + ")";
  }

  public void callTool(RequestContainer request, ResponseContainer response) {

    McpToolsAspect toolAspect = request.getAspect(McpToolsAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing tools aspect"));

    McpSchema.Tool selectedTool = toolAspect.getSelectedMcpTool()
        .orElseThrow(() -> new IllegalArgumentException("No tool selected"));

    McpAsyncClient mcpClient = ((McpResponseContainer) response).getMcpClient();
    mcpClient.callTool(McpSchema.CallToolRequest.builder()
            .name(selectedTool.name())
            .arguments(toolAspect.getQuery())
            .build())
        .subscribe(
            result -> updateResponse(result, response),
            error -> updateErrorResponse(error, response));
  }

  private void updateErrorResponse(Throwable error, ResponseContainer response) {
    McpResponseAspect responseAspect = response.getAspect(McpResponseAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing response aspect"));
    var rootCause = ExceptionUtils.getRootCauseMessage(error);
    responseAspect.setResponse("Failed to call mcp: " + rootCause);
  }

  private static void updateResponse(McpSchema.CallToolResult result, ResponseContainer response) {
    McpResponseAspect responseAspect = response.getAspect(McpResponseAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing response aspect"));
    responseAspect.setResponse(prettyPrintContents(result.content()));

    McpStructuredOutputAspect structuredOutputAspect =
        response.getAspect(McpStructuredOutputAspect.class)
            .orElseThrow(() -> new IllegalArgumentException("Missing structured output aspect"));

    if (result.structuredContent() == null) {
      structuredOutputAspect.setStructuredOutput("No structured content returned.");
    } else {
      try {
        structuredOutputAspect.setStructuredOutput(
            JsonUtil.prettyPrint(result.structuredContent()));
      } catch (JsonProcessingException e) {
        structuredOutputAspect.setStructuredOutput(
            "Failed to pretty print structured content: " + e.getMessage());
      }
    }
  }

  private static String prettyPrintContents(List<McpSchema.Content> contents) {
    StringBuilder sb = new StringBuilder();
    for (McpSchema.Content content : contents) {
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
    return sb.toString();
  }
}
