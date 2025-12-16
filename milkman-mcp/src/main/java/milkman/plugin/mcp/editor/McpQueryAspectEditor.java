package milkman.plugin.mcp.editor;

import static milkman.utils.FunctionalUtils.run;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.javafx.PlatformUtil;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.plugin.mcp.domain.McpQueryAspect;
import milkman.plugin.mcp.domain.McpToolsAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;

@Slf4j
public class McpQueryAspectEditor implements RequestAspectEditor, ContentTypeAwareEditor {

  private List<ContentTypePlugin> contentTypePlugins;

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

    return new

        Tab("Query", splitPane);
  }

  private static void updateInputSchema(McpToolsAspect toolAspect, TextArea inputSchema) {
    toolAspect.getSelectedTool().ifPresent(toolSpec -> {
      ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
		try {
        String prettySchema = writer.writeValueAsString(toolSpec.inputSchema());
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
}
