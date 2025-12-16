package milkman.plugin.mcp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import milkman.domain.RequestContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class McpRequestContainer extends RequestContainer {


  private McpTransportType transport = McpTransportType.Sse;
  private String url;

  @JsonIgnore
  Sinks.Many<McpSyncClient> mcpClient = Sinks.many().replay().latest();

  @JsonIgnore
  public Flux<McpSyncClient> getMcpClient() {
    return mcpClient.asFlux();
  }

  public void setMcpClient(McpSyncClient client) {
    this.mcpClient.tryEmitNext(client);
  }

  @Override
  public String getType() {
    return "Mcp";
  }

  public McpRequestContainer(String name, String url) {
    super(name);
    this.url = url;
  }

}
