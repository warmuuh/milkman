package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.ui.components.JfxTableEditor;
import milkman.utils.fxml.FxmlUtil;

import java.util.UUID;

import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.label;

public class EditEnvironmentDialog {

 	JfxTableEditor<EnvironmentEntry> editor;
	private Dialog dialog;

	public void showAndWait(Environment environment) {
		JFXDialogLayout content = new EditEnvironmentDialogFxml(this);

		editor.enableAddition(() -> new EnvironmentEntry(UUID.randomUUID().toString(), "", "", true));
		editor.addCheckboxColumn("Enabled", EnvironmentEntry::isEnabled, EnvironmentEntry::setEnabled);
		editor.addColumn("name", EnvironmentEntry::getName, EnvironmentEntry::setName);
		editor.addColumn("value", EnvironmentEntry::getValue, EnvironmentEntry::setValue);
		editor.addDeleteColumn("delete");
		editor.setItems(environment.getEntries(), (a,b) -> a.getName().compareTo(b.getName()));
		editor.setRowToStringConverter(e -> e.getName() + ": " + e.getValue());

		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}



	 public void onClose() {
		dialog.close();
	}


	public static class EditEnvironmentDialogFxml extends JFXDialogLayout {

		public EditEnvironmentDialogFxml(EditEnvironmentDialog controller){
			setHeading(label("Edit Environment"));

			var editor = controller.editor = new JfxTableEditor<>();
			editor.setMinHeight(500);
			editor.setMinWidth(800);
			setBody(editor);

			Button close = cancel(controller::onClose, "Close");
			close.getStyleClass().add("dialog-accept");
			setActions(close);
		}

	}



}
