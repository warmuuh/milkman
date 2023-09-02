package milkman.exporter;

import static milkman.utils.fxml.facade.FxmlBuilder.hbox;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.space;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Value;
import milkman.domain.RequestContainer;
import milkman.exporter.CustomTemplateService.CompiledTemplate;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.RequestTypePluginAware;
import milkman.ui.plugin.Templater;
import milkman.utils.fxml.facade.FxmlBuilder;

public class CustomTemplateExporter implements RequestExporterPlugin, RequestTypePluginAware {


  private CustomTemplateService templateService = new CustomTemplateService();
  private TextArea textArea;

  private RequestContainer request;
  private ComboBox<ExportHolder> cbTemplate;
  private List<RequestTypePlugin> registeredRequestTypePlugins;

  @Override
  public String getName() {
    return "Code Templates";
  }

  @Override
  public Node getRoot(RequestContainer request, Templater templater) {
    this.request = request;
    String requestType = registeredRequestTypePlugins.stream().filter(r -> r.canHandle(request))
        .map(r -> r.getRequestType()).findAny().orElse("");
    textArea = new TextArea();
    textArea.setEditable(false);

    cbTemplate = FxmlBuilder.comboBox("templateName");
    List<ExportHolderWrap> userTemplates = templateService.getTemplatesForType(requestType).stream()
        .sorted(Comparator.comparing(CompiledTemplate::getTemplateName))
        .map(ExportHolderWrap::new).collect(Collectors.toList());
    List<ExportHolderWrap> predefTemplates = templateService.getPredefinedTemplates(requestType).stream()
        .sorted(Comparator.comparing(CompiledTemplate::getTemplateName))
        .map(ExportHolderWrap::new)
        .collect(Collectors.toList());

    cbTemplate.getItems().addAll(userTemplates);
    if (!userTemplates.isEmpty() && !predefTemplates.isEmpty()) {
      cbTemplate.getItems().add(new ExportSeparator());
    }
    cbTemplate.getItems().addAll(predefTemplates);

    cbTemplate.valueProperty().addListener((obs, o, n) -> {
      if (n != null) {
        refreshCommand(templater, n.getExporter());
      }
    });
    cbTemplate.getSelectionModel().selectFirst();

    VBox root = new VBox();
    root.getChildren().add(hbox(label("Template: "), space(5), cbTemplate));
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

  @Override
  public void setRequestTypePlugins(List<RequestTypePlugin> plugins) {
    this.registeredRequestTypePlugins = plugins;
  }


  public interface ExportHolder {
    CompiledTemplate getExporter();
  }

  @Value
  public static class ExportHolderWrap implements ExportHolder{
    CompiledTemplate exporter;

    @Override
    public String toString() {
      return exporter.getTemplateName();
    }
  }

  public static class ExportSeparator extends Separator implements ExportHolder {
    @Override
    public CompiledTemplate getExporter() {
      return null;
    }
  }

  @Override
  public int getOrder() {
    return 100;
  }
}
