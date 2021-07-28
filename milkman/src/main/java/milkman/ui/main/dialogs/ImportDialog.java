package milkman.ui.main.dialogs;

import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.BindableComboBox;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder.*;

import java.util.List;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

public class ImportDialog {
  VBox importerArea;
  BindableComboBox<ImporterPlugin> importerSelector;
  private Dialog dialog;

  private ImporterPlugin selectedImporter;
  private Toaster toaster;
  private Workspace workspace;

  public void showAndWait(List<ImporterPlugin> importers, Toaster toaster, Workspace workspace) {
    this.toaster = toaster;
    this.workspace = workspace;
    var content = new ImportDialogFxml(this);
    importerSelector.setPromptText("Select Importer");
    importerSelector.setConverter(new StringConverter<ImporterPlugin>() {

      @Override
      public String toString(ImporterPlugin object) {
        return object.getName();
      }

      @Override
      public ImporterPlugin fromString(String string) {
        return null;
      }
    });
    importers.forEach(i -> importerSelector.getItems().add(i));
    importerSelector.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> {
      if (o != v && v != null) {
        selectedImporter = v;
        importerArea.getChildren().clear();
        importerArea.getChildren().add(v.getImportControls());
      }
    });
    dialog = FxmlUtil.createDialog(content);
    dialog.showAndWait();
  }

  public void onClose() {
    dialog.close();
  }

  public void onImport() {
    if (selectedImporter != null) {
      if (selectedImporter.importInto(workspace, toaster)) {
        dialog.close();
      }
    }
  }

  public class ImportDialogFxml extends DialogLayoutBase {
    public ImportDialogFxml(ImportDialog controller) {
      setHeading(label("Import"));

      var vbox = new VboxExt();
      controller.importerSelector = vbox.add(combobox());
      controller.importerArea = vbox.add(vbox("importerArea"));
      setBody(vbox);

      setActions(submit(controller::onImport, "Import"), cancel(controller::onClose, "Close"));

    }
  }
}
