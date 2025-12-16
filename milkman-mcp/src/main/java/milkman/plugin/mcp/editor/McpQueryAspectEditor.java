package milkman.plugin.mcp.editor;

import static milkman.utils.FunctionalUtils.run;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.JsonUtil;
import milkman.plugin.mcp.McpRequestProcessor;
import milkman.plugin.mcp.domain.McpQueryAspect;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;

@Slf4j
@RequiredArgsConstructor
public class McpQueryAspectEditor implements RequestAspectEditor, ContentTypeAwareEditor,
    ExecutionListenerAware {

  private List<ContentTypePlugin> contentTypePlugins;
  private ExecutionListenerManager executionListenerManager;
  private final McpRequestProcessor mcpRequestProcessor;

  @Override
  @SneakyThrows
  public Tab getRoot(RequestContainer request) {
    val queryAspect = request.getAspect(McpQueryAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Mcp Query Aspect missing"));
    val toolAspect = request.getAspect(McpToolsAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Mcp Tool Aspect missing"));

    TextArea inputSchema = new TextArea();
    inputSchema.setEditable(false);
    inputSchema.setText("No tool selected");
    updateInputSchema(toolAspect, inputSchema);

    // FIXME: currently, we are the only one listening, so we can delete all listeners here
    // but this is not a good long-term solution
    toolAspect.getToolsChanged().clear();
    toolAspect.getToolsChanged().add(() ->
        updateInputSchema(toolAspect, inputSchema));


    ContentEditor editor = new ContentEditor();
    editor.setEditable(true);
    editor.setContent(queryAspect::getQuery,
        run(queryAspect::setQuery).
            andThen(() -> queryAspect.setDirty(true)));
    editor.setContentTypePlugins(contentTypePlugins);
    editor.setContentType("application/json");

    var splitPane = new SplitPane(editor, inputSchema);
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(0.7);


    var addItemBtn = new JFXButton();
    addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE, "1.5em"));
    addItemBtn.getStyleClass().add("btn-add-entry");
    addItemBtn.setDisable(true);

    StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));

    executionListenerManager.listenOnExecution(request, "mcp-msg-sender-listener",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {
          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(false);
            addItemBtn.setOnAction(e -> {
              mcpRequestProcessor.executeRequest(request, response);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(true);
          }
        });

    return new
        Tab("Query", new StackPane(splitPane, addItemBtn));
  }

  private static void updateInputSchema(McpToolsAspect toolAspect, TextArea inputSchema) {
    toolAspect.getSelectedMcpTool().ifPresent(toolSpec -> {
      try {
        String prettySchema = JsonUtil.prettyPrint(toolSpec.inputSchema());
        inputSchema.setText(prettySchema);
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize input schema", e);
      }
    });
  }

  @Override
  public boolean canHandleAspect(RequestContainer request) {
    return request.getAspect(McpQueryAspect.class).isPresent();
  }

  @Override
  public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
    this.contentTypePlugins = plugins;
  }

  @Override
  public void setExecutionListenerManager(ExecutionListenerManager manager) {
    this.executionListenerManager = manager;
  }
}
