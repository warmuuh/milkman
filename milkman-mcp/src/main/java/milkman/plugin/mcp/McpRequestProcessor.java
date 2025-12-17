package milkman.plugin.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpEventsAspect;
import milkman.plugin.mcp.domain.McpInstructionsAspect;
import milkman.plugin.mcp.domain.McpPromptsAspect;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResourcesAspect;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.plugin.mcp.domain.McpResponseContainer;
import milkman.plugin.mcp.domain.McpStructuredOutputAspect;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
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

    Map<String, String> headerMap = headers.getEntries().stream()
        .filter(HeaderEntry::isEnabled)
        .collect(
            Collectors.toMap(
                entry -> templater.replaceTags(entry.getName()),
                entry -> templater.replaceTags(entry.getValue())
            )
        );

    Consumer<HttpRequest.Builder> addHeaders = httpRequest -> {
      headerMap.forEach(httpRequest::header);
    };

    String url = templater.replaceTags(request.getUrl());

    var transport = switch (request.getTransport()) {
      case Sse -> HttpClientSseClientTransport
          .builder(getBaseUri(url))
          .sseEndpoint(getEndpointFromUrl(url))
          .customizeRequest(addHeaders)
          .build();
      case StreamableHttp -> HttpClientStreamableHttpTransport
          .builder(getBaseUri(url))
          .endpoint(getEndpointFromUrl(url))
          .customizeRequest(addHeaders)
          .build();
      case StdIo -> new StdioClientTransport(buildServerParams(url, headerMap));
    };

    transport.setExceptionHandler(t -> {
      log.error("Transport error: ", t);
    });

    McpEventsAspect eventsAspect = new McpEventsAspect();

    McpClient.AsyncSpec clientSpec = McpClient.async(transport);
    registerEventConsumers(clientSpec, eventsAspect);

    McpAsyncClient client = clientSpec
        .initializationTimeout(Duration.ofSeconds(5))
        .build();

    McpResponseContainer responseContainer = new McpResponseContainer();
    responseContainer.setMcpClient(client);


    McpInstructionsAspect instructionsAspect = new McpInstructionsAspect();
    responseContainer.getAspects().add(instructionsAspect);

    McpResponseAspect response = new McpResponseAspect();
    response.setResponse("Trigger request in Tool/Resource/Prompt editor...");
    responseContainer.getAspects().add(response);

    McpStructuredOutputAspect structuredOutputAspect = new McpStructuredOutputAspect();
    structuredOutputAspect.setStructuredOutput("Trigger request in Tool/Resource/Prompt editor...");
    responseContainer.getAspects().add(structuredOutputAspect);

    responseContainer.getAspects().add(eventsAspect);

    asyncControl.onCancellationRequested.add(() -> {
      client.close();
      asyncControl.triggerRequestSucceeded();
    });

    client.initialize().subscribeOn(Schedulers.boundedElastic())
        .subscribe(res -> {
          updateStatusOnInitialization(res, responseContainer);
          if (res.instructions() != null) {
            instructionsAspect.setInstructions(res.instructions());
          }
          asyncControl.triggerReqeuestStarted();
          asyncControl.triggerReqeuestReady();
        }, err -> {
          updateErrorResponse(err, responseContainer);
          asyncControl.triggerRequestFailed(err);
        });

    return responseContainer;
  }

  private String getEndpointFromUrl(String url) {
    URI uri = URI.create(url);
    return uri.getPath();
  }

  private static String getBaseUri(String url) {
    URI uri = URI.create(url);
    return uri.getScheme() + "://" + uri.getHost() +
        (uri.getPort() != -1
            ? ":" + uri.getPort()
            : "");
  }

  private static ServerParameters buildServerParams(
      String url,
      Map<String, String> headers
  ) {
    //split "url" (which we use as argument string) into command and list of args

    var parts = url.split(" ");
    if (parts.length == 0) {
      throw new IllegalArgumentException("No command specified in url field");
    }
    String command = parts[0];
    List<String> args = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));

    return ServerParameters.builder(command)
        .args(args)
        .env(headers)
        .build();
  }

  private static void registerEventConsumers(
      McpClient.AsyncSpec clientSpec, McpEventsAspect eventsAspect) {
    clientSpec
        // TODO: setup consumers and show/trigger changes in UI
        .loggingConsumer(notice -> {
          eventsAspect.addEvent("LOG: [%s] %s -- %s".formatted(
              notice.level(),
              notice.logger(),
              notice.data()
          ));
          return Mono.empty();
        })
        .promptsChangeConsumer(prompts -> {
          // TODO: trigger UI update
          eventsAspect.addEvent("Prompts changed: %s".formatted(
              prompts.stream().map(McpSchema.Prompt::name).toList()
          ));
          return Mono.empty();
        })
        .resourcesChangeConsumer(resources -> {
          // TODO: trigger UI update
          eventsAspect.addEvent("Resources changed: %s".formatted(
              resources.stream().map(McpSchema.Resource::name).toList()
          ));
          return Mono.empty();
        })
        .resourcesUpdateConsumer(resourceContents -> {
          // TODO: trigger UI update
          eventsAspect.addEvent("Resources updated: %s".formatted(
              resourceContents.stream().map(McpSchema.ResourceContents::uri).toList()
          ));
          return Mono.empty();
        })
        .toolsChangeConsumer(tools -> {
          // TODO: trigger UI update
          eventsAspect.addEvent("Tools changed: %s".formatted(
              tools.stream().map(McpSchema.Tool::name).toList()
          ));
          return Mono.empty();
        });
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
            : "none"
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


  public void callPrompt(RequestContainer request, ResponseContainer response) {
    McpRequestContainer mcpRequest = (McpRequestContainer) request;
    McpPromptsAspect promptAspect = mcpRequest.getAspect(McpPromptsAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing prompts aspect"));

    McpSchema.Prompt selectedPrompt = promptAspect.getSelectedMcpPrompt()
        .orElseThrow(() -> new IllegalArgumentException("No prompt selected"));

    Map<String, Object> arguments;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      arguments = objectMapper.readValue(promptAspect.getQuery(), new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(
          "Failed to parse prompt arguments as JSON: " + e.getMessage(), e);
    }


    McpAsyncClient mcpClient = ((McpResponseContainer) response).getMcpClient();
    mcpClient.getPrompt(new McpSchema.GetPromptRequest(
            selectedPrompt.name(),
            arguments
        ))
        .subscribe(
            result -> updatePromptResponse(result, response),
            error -> updateErrorResponse(error, response));
  }

  public void callResource(RequestContainer request, ResponseContainer response) {
    McpRequestContainer mcpRequest = (McpRequestContainer) request;
    McpResourcesAspect resourceAspect = mcpRequest.getAspect(McpResourcesAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing resources aspect"));

    McpSchema.Resource selectedResource = resourceAspect.getSelectedMcpResource()
        .orElseThrow(() -> new IllegalArgumentException("No resource selected"));

    McpAsyncClient mcpClient = ((McpResponseContainer) response).getMcpClient();
    mcpClient.readResource(new McpSchema.ReadResourceRequest(selectedResource.uri()))
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


  private void updatePromptResponse(McpSchema.GetPromptResult result, ResponseContainer response) {
    McpResponseAspect responseAspect = response.getAspect(McpResponseAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing response aspect"));
    responseAspect.setResponse(prettyPrintPromptMessages(result.description(), result.messages()));

    McpStructuredOutputAspect structuredOutputAspect =
        response.getAspect(McpStructuredOutputAspect.class)
            .orElseThrow(() -> new IllegalArgumentException("Missing structured output aspect"));

    structuredOutputAspect.setStructuredOutput("No structured content returned.");
  }


  private static void updateResponse(
      McpSchema.ReadResourceResult result, ResponseContainer response) {
    McpResponseAspect responseAspect = response.getAspect(McpResponseAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing response aspect"));
    responseAspect.setResponse(prettyPrintResourceContents(result.contents()));

    McpStructuredOutputAspect structuredOutputAspect =
        response.getAspect(McpStructuredOutputAspect.class)
            .orElseThrow(() -> new IllegalArgumentException("Missing structured output aspect"));

    structuredOutputAspect.setStructuredOutput("No structured content returned.");
  }

  private static String prettyPrintResourceContents(List<McpSchema.ResourceContents> contents) {
    StringBuilder sb = new StringBuilder();
    for (McpSchema.ResourceContents content : contents) {
      sb.append("Content Type: ").append(content.mimeType()).append("\n");
      switch (content) {
        case McpSchema.TextResourceContents textResource ->
            sb.append(textResource.text()).append("\n");
        case McpSchema.BlobResourceContents embedded -> {
          sb.append("<non-text embedded resource>\n");
        }
        default -> sb.append("<omitted>\n");
      }
    }
    return sb.toString();
  }

  private String prettyPrintPromptMessages(
      String description, List<McpSchema.PromptMessage> messages) {
    StringBuilder sb = new StringBuilder();
    if (description != null) {
      sb.append("Description: ").append(description).append("\n\n");
    }
    for (McpSchema.PromptMessage message : messages) {
      sb.append("Role: ").append(message.role()).append("\n");
      sb.append(prettyPrintContents(List.of(message.content()))).append("\n");
    }
    return sb.toString();
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
