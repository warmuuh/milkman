package milkman.plugin.mcp;

import java.util.List;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResourcesAspect;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.plugin.mcp.domain.McpTransportType;
import milkman.plugin.mcp.editor.McpInstructionsAspectEditor;
import milkman.plugin.mcp.editor.McpRequestEditor;
import milkman.plugin.mcp.editor.McpResourcesAspectEditor;
import milkman.plugin.mcp.editor.McpResponseAspectEditor;
import milkman.plugin.mcp.editor.McpStructuredOutputAspectEditor;
import milkman.plugin.mcp.editor.McpToolsAspectEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.AsyncResponseControl;

public class McpPlugin implements RequestTypePlugin, RequestAspectsPlugin {

  private McpRequestProcessor processor = new McpRequestProcessor();

  @Override
  public List<RequestAspectEditor> getRequestTabs() {
    return List.of(new McpToolsAspectEditor(processor), new McpResourcesAspectEditor(processor));
  }

  @Override
  public List<ResponseAspectEditor> getResponseTabs() {
    return List.of(new McpResponseAspectEditor(), new McpStructuredOutputAspectEditor(), new McpInstructionsAspectEditor());
  }

  @Override
  public void initializeRequestAspects(RequestContainer request) {
    if (request instanceof McpRequestContainer) {
      if (!request.getAspect(McpToolsAspect.class).isPresent()) {
        request.addAspect(new McpToolsAspect());
      }
      if (!request.getAspect(McpResourcesAspect.class).isPresent()) {
        request.addAspect(new McpResourcesAspect());
      }
      if (!request.getAspect(RestHeaderAspect.class).isPresent()) {
        request.addAspect(new RestHeaderAspect());
      }
    }
  }

  @Override
  public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
    // we did it on our own, so nothing to do
  }

  @Override
  public RequestContainer createNewRequest() {
    return new McpRequestContainer("New Mcp Request", McpTransportType.Sse,"");
  }

  @Override
  public RequestTypeEditor getRequestEditor() {
    return new McpRequestEditor();
  }

  @Override
  public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater,
      AsyncResponseControl.AsyncControl asyncControl) {
    return processor.connect((McpRequestContainer) request, templater, asyncControl);
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
