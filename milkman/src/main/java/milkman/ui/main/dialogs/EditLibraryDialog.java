package milkman.ui.main.dialogs;

import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.hbox;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.submit;
import static milkman.utils.fxml.facade.FxmlBuilder.text;
import static milkman.utils.fxml.facade.FxmlBuilder.vbox;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import lombok.Getter;
import milkman.ui.main.library.LibraryOptionsProvider.LibraryDefinition;
import milkman.ui.plugin.LibraryPlugin;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.BindableComboBox;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder;

public class EditLibraryDialog {

  private Dialog dialog;
  @Getter
  boolean cancelled = true;

  BindableComboBox<String> libraryTypeSelection;
  TextField libraryUrl;

  public EditLibraryDialog() {
  }

  public void showAndWait(Optional<LibraryDefinition> libraryDefinition, List<LibraryPlugin> libraryPlugins) {
    DialogLayoutBase content = new EditLibraryDialog.EditLibraryDialogFxml(this);
    List<String> typeNames = libraryPlugins.stream().map(LibraryPlugin::getName).collect(Collectors.toList());
    libraryTypeSelection.getItems().addAll(typeNames);
    if (libraryPlugins.size() > 0) {
      libraryTypeSelection.setValue(libraryPlugins.get(0).getName());
      libraryUrl.setText(libraryPlugins.get(0).getDefaultUrl());
    }

    libraryDefinition.ifPresent(ld -> {
      libraryTypeSelection.setValue(ld.getType());
      libraryUrl.setText(ld.getUrl());
    });

    dialog = FxmlUtil.createDialog(content);
    dialog.showAndWait();
  }

  public String getLibraryType() {
    return libraryTypeSelection.getValue();
  }

  public String getLibraryUrl() {
    return libraryUrl.getText();
  }

  private void onSave() {
    cancelled = false;
    dialog.close();
  }

  private void onCancel() {
    cancelled = true;
    dialog.close();
  }

  public static class EditLibraryDialogFxml extends DialogLayoutBase {

    public EditLibraryDialogFxml(EditLibraryDialog controller) {
      setHeading(label("Edit Library"));

      setBody(vbox(
              hbox(
                  label("Library Type"),
                  controller.libraryTypeSelection = FxmlBuilder.combobox()
              ),
              hbox(
                  label("Library Url"),
                  controller.libraryUrl = text("library.url", "Url of library")
              )
          )
      );

      setActions(submit(controller::onSave), cancel(controller::onCancel));
    }
  }
}
