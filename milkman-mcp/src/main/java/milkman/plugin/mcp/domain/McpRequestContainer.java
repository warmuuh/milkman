package milkman.plugin.mcp.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import milkman.domain.RequestContainer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class McpRequestContainer extends RequestContainer {


  private McpTransportType transport = McpTransportType.Sse;
  private String url;

  @Override
  public String getType() {
    return "Mcp";
  }

  public McpRequestContainer(String name, String url) {
    super(name);
    this.url = url;
  }

}
