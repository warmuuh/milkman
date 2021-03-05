package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.submit;

public class EditKeyDialog {

	private Dialog dialog;
	private KeyEditor<?> editor;


	public EditKeyDialog() {}

	public void showAndWait(KeyEntry keyEntry, KeyEditor editor) {
		this.editor = editor;
		JFXDialogLayout content = new EditKeyDialogFxml(this, editor.getRoot(keyEntry));
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	 private void onSave() {
		dialog.close();
	}

	public static class EditKeyDialogFxml extends JFXDialogLayout {
		public EditKeyDialogFxml(EditKeyDialog controller, Node body){
			setHeading(label("Edit Key"));
			setBody(body);
			setActions(submit(controller::onSave));
		}
	}
}
