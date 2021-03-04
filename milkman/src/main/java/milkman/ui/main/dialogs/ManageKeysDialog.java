package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Dialog;
import milkman.domain.KeySet;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.components.JfxTableEditor;
import milkman.utils.fxml.FxmlUtil;

import java.util.Comparator;

import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.label;

public class ManageKeysDialog {


	JfxTableEditor<KeyEntry> editor;
	private Dialog dialog;

	public void showAndWait(KeySet keySet) {
		JFXDialogLayout content = new ManageKeysDialogFxml(this);

		editor.addColumn("name", KeyEntry::getName, KeyEntry::setName);
		editor.addReadOnlyColumn("value", KeyEntry::getValue);
		editor.addDeleteColumn("delete");
		editor.setItems(keySet.getEntries(), Comparator.comparing(KeyEntry::getName));
		editor.setRowToStringConverter(e -> e.getName() + ": " + e.getValue());

		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}



	public void onClose() {
		dialog.close();
	}


	public static class ManageKeysDialogFxml extends JFXDialogLayout {

		public ManageKeysDialogFxml(ManageKeysDialog controller){
			setHeading(label("Edit Key-Set"));

			var editor = controller.editor = new JfxTableEditor<>();
			editor.setMinHeight(500);
			editor.setMinWidth(800);
			setBody(editor);

			JFXButton close = cancel(controller::onClose, "Close");
			close.getStyleClass().add("dialog-accept");
			setActions(close);
		}

	}

}
