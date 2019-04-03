package milkman.ui.main.dialogs;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.stage.Stage;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.components.TableEditor;
import milkman.utils.fxml.FxmlUtil;

public class EditEnvironmentDialog {

	@FXML JfxTableEditor<EnvironmentEntry> editor;
	private Stage dialog;

	public void showAndWait(Environment environment) {
		Parent content = FxmlUtil.loadAndInitialize("/dialogs/EditEnvironmentDialog.fxml", this);
		
		editor.setEditable(true);
		editor.addCheckboxColumn("Enabled", EnvironmentEntry::isEnabled, EnvironmentEntry::setEnabled);
		editor.addColumn("name", EnvironmentEntry::getName, EnvironmentEntry::setName);
		editor.addColumn("value", EnvironmentEntry::getValue, EnvironmentEntry::setValue);
		editor.addDeleteColumn("delete");
		editor.setItems(environment.getEntries(), () -> new EnvironmentEntry("", "", true));
		
		dialog = FxmlUtil.createDialog("Edit Environment", content);
		dialog.showAndWait();
	}

	
	
	@FXML public void onClose() {
		dialog.close();
	}

	
	
	
}
