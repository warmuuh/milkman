package milkman.ui.main.dialogs;

import java.util.Collections;

import com.jfoenix.controls.JFXDialogLayout;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.components.TableEditor;
import milkman.utils.fxml.FxmlUtil;

public class EditEnvironmentDialog {

	@FXML JfxTableEditor<EnvironmentEntry> editor;
	private Dialog dialog;

	public void showAndWait(Environment environment) {
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/EditEnvironmentDialog.fxml", this);
		
		editor.enableEdition(() -> new EnvironmentEntry("", "", true));
		editor.addCheckboxColumn("Enabled", EnvironmentEntry::isEnabled, EnvironmentEntry::setEnabled);
		editor.addColumn("name", EnvironmentEntry::getName, EnvironmentEntry::setName);
		editor.addColumn("value", EnvironmentEntry::getValue, EnvironmentEntry::setValue);
		editor.addDeleteColumn("delete");
		editor.setItems(environment.getEntries());
		
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	
	
	@FXML public void onClose() {
		dialog.close();
	}

	
	
	
}
