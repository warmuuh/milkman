package milkman.exporter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import milkman.ui.main.dialogs.EditTemplateDialog;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class CustomTemplateOptionsProvider implements OptionPageProvider<CustomTemplateOptionsProvider.CustomTemplateOptions> {

  @Data
  public static class CustomTemplateOptions implements OptionsObject {
    private List<ExportTemplate> exportTemplates = new LinkedList<>();
  }

  private static CustomTemplateOptions currentOptions = new CustomTemplateOptions();
  public static CustomTemplateOptions options() {
    return currentOptions;
  }

  @Override
  public CustomTemplateOptions getOptions() {
    return currentOptions;
  }

  @Override
  public void setOptions(CustomTemplateOptions options) {
    currentOptions = options;
  }

  @Override
  public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
    return builder.page("Templates", getOptions())
        .section("Custom Templates")
        .list(
            CustomTemplateOptions::getExportTemplates,
            Map.of(
                "Name", ExportTemplate::getName,
                "Type", ExportTemplate::getRequestType
            ),
            this::createTemplate,
            this::editTemplate
        )
        .endSection()
        .build();
  }

  private void editTemplate(ExportTemplate template) {
    EditTemplateDialog dialog = new EditTemplateDialog();
    dialog.showAndWait(template.getName(), template.getRequestType(), template.getTemplate());
    if (!dialog.isCancelled()) {
      template.setName(dialog.getName());
      template.setRequestType(dialog.getRequestType());
      template.setTemplate(dialog.getTemplate());
    }
  }

  private ExportTemplate createTemplate() {
    EditTemplateDialog dialog = new EditTemplateDialog();
    dialog.showAndWait("new template", "HTTP", "");
    if (dialog.isCancelled()) {
      return null;
    }

    return new ExportTemplate(dialog.getName(), dialog.getRequestType(), dialog.getTemplate());
  }

  @Override
  public int getOrder() {
    return 500;
  }
}