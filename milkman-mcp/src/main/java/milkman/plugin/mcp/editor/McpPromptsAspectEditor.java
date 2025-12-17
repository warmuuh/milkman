package milkman.plugin.mcp.editor;

import static milkman.utils.FunctionalUtils.run;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
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
import milkman.plugin.mcp.domain.McpPromptsAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.ToasterAware;

@Slf4j
@RequiredArgsConstructor
public class McpPromptsAspectEditor implements RequestAspectEditor, ToasterAware,
    ExecutionListenerAware, ContentTypeAwareEditor {
  private Toaster toaster;
  private ExecutionListenerManager executionListenerManager;
  private List<ContentTypePlugin> contentTypePlugins;
  private ListView<McpSchema.Prompt> promptList;
  private final McpRequestProcessor mcpRequestProcessor;
  private TextArea inputSchema;

  @Override
  public Tab getRoot(RequestContainer request) {
    return getRoot(request, Optional.empty());
  }

  @Override
  public Tab getRoot(RequestContainer request, Optional<ResponseContainer> response) {
    this.promptList = new ListView<McpSchema.Prompt>();
    McpPromptsAspect promptsAspect = request.getAspect(McpPromptsAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Mcp Prompt Aspect missing"));
    promptList.getItems().addAll(promptsAspect.getPrompts());
    promptList.setCellFactory(param -> new McpPromptListCell());

    var descriptionArea = new TextArea();
    descriptionArea.setEditable(false);

    promptList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newPrompt) -> {
      if (newPrompt != null && !newPrompt.name().equals(promptsAspect.getSelectedPrompt())) {
        promptsAspect.setSelectedPrompt(newPrompt.name());
        updateInputSchema(promptsAspect);
        request.setDirty(true);
      }
      descriptionArea.setText(newPrompt != null ? newPrompt.description() : "");
    });

    response.ifPresent(resp -> updatePromptList((McpResponseContainer) resp, promptsAspect));
    registerUpdateListener((McpRequestContainer) request, promptList, promptsAspect);

    promptsAspect.getSelectedMcpPrompt().ifPresent(prompt -> {
      Platform.runLater(() -> {
        promptList.getSelectionModel().select(prompt);
      });
    });


    this.inputSchema = new TextArea();
    inputSchema.setEditable(false);
    inputSchema.setText("No prompt selected");
    updateInputSchema(promptsAspect);

    ContentEditor editor = new ContentEditor();
    editor.setEditable(true);
    editor.setContent(promptsAspect::getQuery,
        run(promptsAspect::setQuery).
            andThen(() -> promptsAspect.setDirty(true)));
    editor.setContentTypePlugins(contentTypePlugins);
    editor.setContentType("application/json");

    var editorSplitPane = new SplitPane(editor, inputSchema);
    editorSplitPane.setOrientation(Orientation.HORIZONTAL);
    editorSplitPane.setDividerPositions(0.7);

    var promptSplitPane = new SplitPane(promptList, descriptionArea);
    promptSplitPane.setOrientation(Orientation.VERTICAL);
    promptSplitPane.setDividerPositions(0.8);

    var splitPane = new SplitPane(promptSplitPane, editorSplitPane);
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(0.2);

    var addItemBtn = new JFXButton();
    addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE, "1.5em"));
    addItemBtn.getStyleClass().add("btn-add-entry");
    addItemBtn.setDisable(true);
    StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));

    executionListenerManager.listenOnExecution(request, "mcp-msg-prompt-listener",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {
          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(false);
            addItemBtn.setOnAction(e -> {
              mcpRequestProcessor.callPrompt(request, response);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(true);
          }
        });

    return new Tab("Prompts", new StackPane(splitPane, addItemBtn));
  }

  private void updateInputSchema(McpPromptsAspect promptAspect) {
    promptAspect.getSelectedMcpPrompt().ifPresent(promptSpec -> {
      try {
        String prettySchema = JsonUtil.prettyPrint(promptSpec.arguments());
        inputSchema.setText(prettySchema);
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize input schema", e);
      }
    });
  }

  private void registerUpdateListener(
      McpRequestContainer request, ListView<McpSchema.Prompt> list, McpPromptsAspect promptsAspect) {
    executionListenerManager.listenOnExecution(request, "mcp-prompt-list-updater",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {

          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            Platform.runLater(() -> {
              updatePromptList((McpResponseContainer) response, promptsAspect);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {

          }
        });
  }

  private void updatePromptList(McpResponseContainer response, McpPromptsAspect promptsAspect) {
    McpAsyncClient mcpClient = response.getMcpClient();
    promptList.getItems().clear();
    try {
      log.info("Fetching MCP prompts..., client: {}", mcpClient);
      McpSchema.ListPromptsResult result = mcpClient.listPrompts().block();
      promptsAspect.setPrompts(result.prompts());
      promptList.getItems().setAll(promptsAspect.getPrompts());

      promptsAspect.getSelectedMcpPrompt().ifPresent(prompt -> {
        Platform.runLater(() -> {
          promptList.getSelectionModel().select(prompt);
        });
      });
      updateInputSchema(promptsAspect);

    } catch (Exception e) {
      toaster.showToast("Failed to fetch MCP prompts: " + e.getMessage());

    }
  }

  @Override
  public boolean canHandleAspect(RequestContainer request) {
    return request.getAspect(McpPromptsAspect.class).isPresent();
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

  private class McpPromptListCell extends ListCell<McpSchema.Prompt> {
    @Override
    protected void updateItem(McpSchema.Prompt item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
      } else {
        setText(item.name());
      }
    }
  }
}
