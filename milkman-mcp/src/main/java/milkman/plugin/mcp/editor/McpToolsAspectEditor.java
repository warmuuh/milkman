package milkman.plugin.mcp.editor;

import static milkman.utils.FunctionalUtils.run;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.SplitPane;
import javafx.geometry.Orientation;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.JsonUtil;
import milkman.plugin.mcp.McpRequestProcessor;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpResponseContainer;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.ToasterAware;

@Slf4j
@RequiredArgsConstructor
public class McpToolsAspectEditor implements RequestAspectEditor, ToasterAware,
    ExecutionListenerAware, ContentTypeAwareEditor {
  private Toaster toaster;
  private ExecutionListenerManager executionListenerManager;
  private List<ContentTypePlugin> contentTypePlugins;
  private ListView<McpSchema.Tool> toolList;
  private final McpRequestProcessor mcpRequestProcessor;
  private ContentEditor inputSchema;

  @Override
  public Tab getRoot(RequestContainer request) {
    return getRoot(request, Optional.empty());
  }

  @Override
  public Tab getRoot(RequestContainer request, Optional<ResponseContainer> response) {
    this.toolList = new JFXListView<>();
    McpToolsAspect toolsAspect = request.getAspect(McpToolsAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Mcp Tool Aspect missing"));
    toolList.getItems().addAll(toolsAspect.getTools());
    toolList.setCellFactory(param -> new McpToolListCell());

    var descriptionArea = new TextArea();
    descriptionArea.setEditable(false);
    descriptionArea.getStyleClass().add("disabled");

    toolList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newTool) -> {
      if (newTool != null && !newTool.name().equals(toolsAspect.getSelectedTool())) {
        toolsAspect.setSelectedTool(newTool.name());
        updateInputSchema(toolsAspect);
        request.setDirty(true);
      }
      descriptionArea.setText(newTool != null ? newTool.description() : "");
    });

    response.ifPresent(resp -> updateToolList((McpResponseContainer) resp, toolsAspect));
    registerUpdateListener((McpRequestContainer) request, toolList, toolsAspect);

    toolsAspect.getSelectedMcpTool().ifPresent(tool -> {
      Platform.runLater(() -> {
        toolList.getSelectionModel().select(tool);
      });
    });


    this.inputSchema = new ContentEditor();
    inputSchema.setEditable(false);
    inputSchema.setDisableContent(true);
    inputSchema.setContent(() -> "No tool selected", s -> {});
    inputSchema.setContentTypePlugins(contentTypePlugins);
    inputSchema.setContentType("application/json");
    inputSchema.setHeaderVisibility(false);
    inputSchema.showLineNumbers(false);
    updateInputSchema(toolsAspect);

    ContentEditor editor = new ContentEditor();
    editor.setEditable(true);
    editor.setContent(toolsAspect::getQuery,
        run(toolsAspect::setQuery).
            andThen(() -> toolsAspect.setDirty(true)));
    editor.setContentTypePlugins(contentTypePlugins);
    editor.setContentType("application/json");

    var editorSplitPane = new SplitPane(editor, inputSchema);
    editorSplitPane.setOrientation(Orientation.HORIZONTAL);
    editorSplitPane.setDividerPositions(0.7);

    var toolSplitPane = new SplitPane(toolList, descriptionArea);
    toolSplitPane.setOrientation(Orientation.VERTICAL);
    toolSplitPane.setDividerPositions(0.8);

    var splitPane = new SplitPane(toolSplitPane, editorSplitPane);
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(0.2);

    var addItemBtn = new JFXButton();
    addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE, "1.5em"));
    addItemBtn.getStyleClass().add("btn-add-entry");
    addItemBtn.setDisable(true);
    StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));

    executionListenerManager.listenOnExecution(request, "mcp-msg-tool-listener",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {
          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(false);
            addItemBtn.setOnAction(e -> {
              mcpRequestProcessor.callTool(request, response);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(true);
          }
        });

    return new Tab("Tools", new StackPane(splitPane, addItemBtn));
  }

  private void updateInputSchema(McpToolsAspect toolAspect) {
    toolAspect.getSelectedMcpTool().ifPresent(toolSpec -> {
      try {
        String prettySchema = JsonUtil.prettyPrint(toolSpec.inputSchema());
        inputSchema.setContent(() -> prettySchema, s -> {});
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize input schema", e);
      }
    });
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
              updateToolList((McpResponseContainer) response, toolsAspect);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {

          }
        });
  }

  private void updateToolList(McpResponseContainer response, McpToolsAspect toolsAspect) {
    McpAsyncClient mcpClient = response.getMcpClient();
    toolList.getItems().clear();
    try {
      log.info("Fetching MCP tools..., client: {}", mcpClient);
      McpSchema.ListToolsResult result = mcpClient.listTools().block();
      toolsAspect.setTools(result.tools());
      toolList.getItems().setAll(toolsAspect.getTools());

      toolsAspect.getSelectedMcpTool().ifPresent(tool -> {
        Platform.runLater(() -> {
          toolList.getSelectionModel().select(tool);
        });
      });
      updateInputSchema(toolsAspect);

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


  @Override
  public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
    this.contentTypePlugins = plugins;
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
