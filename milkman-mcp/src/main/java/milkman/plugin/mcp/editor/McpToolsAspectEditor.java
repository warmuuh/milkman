package milkman.plugin.mcp.editor;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.SplitPane;
import javafx.geometry.Orientation;
import lombok.extern.slf4j.Slf4j;
import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResponseContainer;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.ToasterAware;

@Slf4j
public class McpToolsAspectEditor implements RequestAspectEditor, ToasterAware,
    ExecutionListenerAware {
  private Toaster toaster;
  private ExecutionListenerManager executionListenerManager;

  @Override
  public Tab getRoot(RequestContainer request) {
    return getRoot(request, Optional.empty());
  }

  @Override
  public Tab getRoot(RequestContainer request, Optional<ResponseContainer> response) {
    var list = new ListView<McpSchema.Tool>();
    McpToolsAspect toolsAspect = request.getAspect(McpToolsAspect.class).get();
    list.getItems().addAll(toolsAspect.getTools());
    list.setCellFactory(param -> new McpToolListCell());

    var descriptionArea = new TextArea();
    descriptionArea.setEditable(false);

    list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newTool) -> {
      if (newTool != null && !newTool.name().equals(toolsAspect.getSelectedTool())) {
        toolsAspect.setSelectedTool(newTool.name());
        request.setDirty(true);
      }
      descriptionArea.setText(newTool != null ? newTool.description() : "");
    });

    response.ifPresent(resp -> updateToolList((McpResponseContainer) resp, list, toolsAspect));
    registerUpdateListener((McpRequestContainer) request, list, toolsAspect);

    toolsAspect.getSelectedMcpTool().ifPresent(tool -> {
      Platform.runLater(() -> {
        list.getSelectionModel().select(tool);
      });
    });




    var splitPane = new SplitPane(list, descriptionArea);
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(0.2);

    return new Tab("Tools", splitPane);
  }

  private void registerUpdateListener(
      McpRequestContainer request, ListView<McpSchema.Tool> list, McpToolsAspect toolsAspect) {
    executionListenerManager.listenOnExecution(request, "mcp-tool-list-updater",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {

          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            Platform.runLater(() -> {
              updateToolList((McpResponseContainer) response, list, toolsAspect);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {

          }
        });
  }

  private void updateToolList(
      McpResponseContainer response, ListView<McpSchema.Tool> list, McpToolsAspect toolsAspect) {
    McpAsyncClient mcpClient = ((McpResponseContainer) response).getMcpClient();
    list.getItems().clear();
    try {
      log.info("Fetching MCP tools..., client: {}", mcpClient);
      McpSchema.ListToolsResult result = mcpClient.listTools().block();
      toolsAspect.setTools(result.tools());
      list.getItems().setAll(toolsAspect.getTools());

      toolsAspect.getSelectedMcpTool().ifPresent(tool -> {
        Platform.runLater(() -> {
          list.getSelectionModel().select(tool);
          toolsAspect.getToolsChanged().invoke();
        });
      });
    } catch (Exception e) {
      toaster.showToast("Failed to fetch MCP tools: " + e.getMessage());

    }
  }

  @Override
  public boolean canHandleAspect(RequestContainer request) {
    return request.getAspect(McpToolsAspect.class).isPresent();
  }

  @Override
  public void setToaster(Toaster toaster) {
    this.toaster = toaster;
  }

  @Override
  public void setExecutionListenerManager(ExecutionListenerManager manager) {
    this.executionListenerManager = manager;
  }

  private class McpToolListCell extends ListCell<McpSchema.Tool> {
    @Override
    protected void updateItem(McpSchema.Tool item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
      } else {
        setText(item.name());
      }
    }
  }
}
