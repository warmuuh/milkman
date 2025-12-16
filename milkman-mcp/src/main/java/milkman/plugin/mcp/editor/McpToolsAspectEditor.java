package milkman.plugin.mcp.editor;

import io.modelcontextprotocol.client.McpSyncClient;
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
import milkman.domain.RequestContainer;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.ToasterAware;
import milkman.utils.fxml.FxmlUtil;

@Slf4j
public class McpToolsAspectEditor implements RequestAspectEditor, ToasterAware {
  private Toaster toaster;

  @Override
  public Tab getRoot(RequestContainer request) {
    var list = new ListView<McpSchema.Tool>();
    McpToolsAspect toolsAspect = request.getAspect(McpToolsAspect.class).get();
    list.getItems().addAll(toolsAspect.getTools());
    list.setCellFactory(param -> new McpToolListCell());

    registerUpdateListener((McpRequestContainer) request, list, toolsAspect);

    var descriptionArea = new TextArea();
    descriptionArea.setEditable(false);


    list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newTool) -> {
      if (newTool != null) {
        toolsAspect.setSelectedTool(newTool.name());
      }
      descriptionArea.setText(newTool != null ? newTool.description() : "");
    });

    toolsAspect.getSelectedTool().ifPresent(tool -> {
      Platform.runLater(() -> {
        list.getSelectionModel().select(tool);
        descriptionArea.setText(tool.description());
      });
    });

    var splitPane = new SplitPane(list, descriptionArea);
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(0.2);

    return new Tab("Tools", splitPane);
  }

  private void registerUpdateListener(
      McpRequestContainer request, ListView<McpSchema.Tool> list, McpToolsAspect toolsAspect) {
    request.getMcpClient().subscribe(mcpClient -> {
      Platform.runLater(() ->
          updateToolList(mcpClient, list, toolsAspect));
    });
  }

  private void updateToolList(
      McpSyncClient mcpClient, ListView<McpSchema.Tool> list, McpToolsAspect toolsAspect) {
    list.getItems().clear();
    try {
      log.info("Fetching MCP tools..., client: {}", mcpClient);
      McpSchema.ListToolsResult result = mcpClient.listTools();
      toolsAspect.setTools(result.tools());
      list.getItems().setAll(toolsAspect.getTools());
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
