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
import milkman.plugin.mcp.domain.McpResourcesAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.ToasterAware;
import milkman.utils.fxml.FxmlBuilder;

@Slf4j
@RequiredArgsConstructor
public class McpResourcesAspectEditor implements RequestAspectEditor, ToasterAware,
    ExecutionListenerAware, ContentTypeAwareEditor {
  private Toaster toaster;
  private ExecutionListenerManager executionListenerManager;
  private ListView<McpSchema.Resource> resourceList;
  private final McpRequestProcessor mcpRequestProcessor;
  private ContentEditor inputSchema;
  private List<ContentTypePlugin> contentTypePlugins;

  @Override
  public Tab getRoot(RequestContainer request) {
    return getRoot(request, Optional.empty());
  }

  @Override
  public Tab getRoot(RequestContainer request, Optional<ResponseContainer> response) {
    this.resourceList = new JFXListView<>();
    McpResourcesAspect resourcesAspect = request.getAspect(McpResourcesAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Mcp Resource Aspect missing"));
    resourceList.getItems().addAll(resourcesAspect.getResources());
    resourceList.setCellFactory(param -> new McpResourceListCell());

    var descriptionArea = new TextArea();
    descriptionArea.setEditable(false);
    descriptionArea.getStyleClass().add("disabled");

    resourceList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newResource) -> {
      if (newResource != null && !newResource.name().equals(resourcesAspect.getSelectedResource())) {
        resourcesAspect.setSelectedResource(newResource.name());
        updateInputSchema(resourcesAspect);
        request.setDirty(true);
      }
      descriptionArea.setText(newResource != null ? newResource.description() : "");
    });

    response.ifPresent(resp -> updateResourceList((McpResponseContainer) resp, resourcesAspect));
    registerUpdateListener((McpRequestContainer) request, resourceList, resourcesAspect);

    resourcesAspect.getSelectedMcpResource().ifPresent(resource -> {
      Platform.runLater(() -> {
        resourceList.getSelectionModel().select(resource);
      });
    });


    this.inputSchema = new ContentEditor();
    inputSchema.setEditable(false);
    inputSchema.setDisableContent(true);
    inputSchema.setContent(() -> "No resource selected", s -> {});
    inputSchema.setContentTypePlugins(contentTypePlugins);
    inputSchema.setContentType("application/json");
    inputSchema.setHeaderVisibility(false);
    inputSchema.showLineNumbers(false);
    updateInputSchema(resourcesAspect);


    var resourceSplitPane = new SplitPane(resourceList, descriptionArea);
    resourceSplitPane.setOrientation(Orientation.VERTICAL);
    resourceSplitPane.setDividerPositions(0.8);

    var splitPane = new SplitPane(resourceSplitPane, inputSchema);
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(0.2);

    var addItemBtn = new JFXButton();
    addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE, "1.5em"));
    addItemBtn.getStyleClass().add("btn-add-entry");
    addItemBtn.setDisable(true);
    StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));

    executionListenerManager.listenOnExecution(request, "mcp-msg-resources-listener",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {
          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(false);
            addItemBtn.setOnAction(e -> {
              mcpRequestProcessor.callResource(request, response);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {
            addItemBtn.setDisable(true);
          }
        });

    return new Tab("Resources", new StackPane(splitPane, addItemBtn));
  }

  private void updateInputSchema(McpResourcesAspect resourceAspect) {
    resourceAspect.getSelectedMcpResource().ifPresent(resourceSpec -> {
      try {
        String prettySchema = JsonUtil.prettyPrint(resourceSpec);
        inputSchema.setContent(() -> prettySchema, s -> {});
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize resource spec", e);
      }
    });
  }

  private void registerUpdateListener(
      McpRequestContainer request, ListView<McpSchema.Resource> list, McpResourcesAspect resourcesAspect) {
    executionListenerManager.listenOnExecution(request, "mcp-resource-list-updater",
        new ExecutionListener() {
          @Override
          public void onRequestStarted(RequestContainer request, ResponseContainer response) {

          }

          @Override
          public void onRequestReady(RequestContainer request, ResponseContainer response) {
            Platform.runLater(() -> {
              updateResourceList((McpResponseContainer) response, resourcesAspect);
            });
          }

          @Override
          public void onRequestFinished(RequestContainer request, ResponseContainer response) {

          }
        });
  }

  private void updateResourceList(McpResponseContainer response, McpResourcesAspect resourcesAspect) {
    McpAsyncClient mcpClient = response.getMcpClient();
    resourceList.getItems().clear();
    try {
      log.info("Fetching MCP resources..., client: {}", mcpClient);
      McpSchema.ListResourcesResult result = mcpClient.listResources().block();
      if (result != null) {
        resourcesAspect.setResources(result.resources());
      }
      resourceList.getItems().setAll(resourcesAspect.getResources());

      resourcesAspect.getSelectedMcpResource().ifPresent(resource -> {
        Platform.runLater(() -> {
          resourceList.getSelectionModel().select(resource);
        });
      });
      updateInputSchema(resourcesAspect);

    } catch (Exception e) {
      toaster.showToast("Failed to fetch MCP resources: " + e.getMessage());

    }
  }

  @Override
  public boolean canHandleAspect(RequestContainer request) {
    return request.getAspect(McpResourcesAspect.class).isPresent();
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

  private class McpResourceListCell extends ListCell<McpSchema.Resource> {
    @Override
    protected void updateItem(McpSchema.Resource item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
      } else {
        setText(item.name());
      }
    }
  }
}
