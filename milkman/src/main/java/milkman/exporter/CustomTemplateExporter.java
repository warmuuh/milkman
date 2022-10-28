package milkman.exporter;

import static milkman.utils.fxml.FxmlBuilder.choiceBox;
import static milkman.utils.fxml.FxmlBuilder.vbox;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.exporter.CustomTemplateService.CompiledTemplate;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.Templater;


public class CustomTemplateExporter implements RequestExporterPlugin {


  private CustomTemplateService templateService = new CustomTemplateService();
  private TextArea textArea;

  private RequestContainer request;
  private List<CompiledTemplate> exporters;
  private ChoiceBox<CompiledTemplate> cbTemplate;

  @Override
  public String getName() {
    return "Custom Templates";
  }

  @Override
  public Node getRoot(RequestContainer request, Templater templater) {
    this.request = request;
    this.exporters = templateService.getTemplatesForType(request.getType());
    textArea = new TextArea();
    textArea.setEditable(false);

    cbTemplate = choiceBox("templateName");
    cbTemplate.getItems().addAll(exporters);
    cbTemplate.valueProperty().addListener((obs, o, n) -> {
      if (n != null) {
        refreshCommand(templater, n);
      }
    });

    VBox root = new VBox();
    root.getChildren().add(cbTemplate);
    root.getChildren().add(textArea);
    VBox.setVgrow(textArea, Priority.ALWAYS);
    return 	root;
  }

  public void refreshCommand(Templater templater, CompiledTemplate template) {
    try{
      String renderedValue = template.render(request);
      textArea.setText(templater.replaceTags(renderedValue));
    } catch (Exception e) {
      textArea.setText("Failed to render template: \n" + e.getMessage());
    }
  }


  @Override
  public boolean isAdhocExporter() {
    return true;
  }

  @Override
  public boolean doExport(RequestContainer collection, Templater templater, Toaster toaster) {
    throw new UnsupportedOperationException("not implemented bc adhoc exporter");
  }

  @Override
  public boolean canHandle(RequestContainer request) {
    return templateService.canHandleRequestType(request.getType());
  }
}
