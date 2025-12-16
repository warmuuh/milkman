package milkman.plugin.mcp;

import java.util.List;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpQueryAspect;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.plugin.mcp.editor.McpQueryAspectEditor;
import milkman.plugin.mcp.editor.McpRequestEditor;
import milkman.plugin.mcp.editor.McpResponseAspectEditor;
import milkman.plugin.mcp.editor.McpToolsAspectEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;

public class McpPlugin implements RequestTypePlugin, RequestAspectsPlugin {

  private McpRequestProcessor processor = new McpRequestProcessor();

  @Override
  public List<RequestAspectEditor> getRequestTabs() {
    return List.of(new McpToolsAspectEditor(), new McpQueryAspectEditor());
  }

  @Override
  public List<ResponseAspectEditor> getResponseTabs() {
    return List.of(new McpResponseAspectEditor());
  }

  @Override
  public void initializeRequestAspects(RequestContainer request) {
    if (request instanceof McpRequestContainer) {
      if (!request.getAspect(McpQueryAspect.class).isPresent()) {
        request.addAspect(new McpQueryAspect());
      }
      if (!request.getAspect(McpToolsAspect.class).isPresent()) {
        request.addAspect(new McpToolsAspect());
      }
    }
  }

  @Override
  public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
    // we did it on our own, so nothing to do
  }

  @Override
  public RequestContainer createNewRequest() {
    return new McpRequestContainer("New Mcp Request", "");
  }

  @Override
  public RequestTypeEditor getRequestEditor() {
    return new McpRequestEditor();
  }

  @Override
  public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
    return processor.connect((McpRequestContainer) request, templater);
  }

  @Override
  public String getRequestType() {
    return "Mcp";
  }

  @Override
  public boolean canHandle(RequestContainer request) {
    return request instanceof McpRequestContainer;
  }

  @Override
  public int getOrder() {
    return 33;
  }
}
