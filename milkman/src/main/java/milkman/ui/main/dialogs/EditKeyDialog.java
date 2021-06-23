package milkman.ui.main.dialogs;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;

import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.submit;

public class EditKeyDialog {

	private Dialog dialog;
	private KeyEditor<?> editor;


	public EditKeyDialog() {}

	public void showAndWait(KeyEntry keyEntry, KeyEditor editor) {
		this.editor = editor;
		var content = new EditKeyDialogFxml(this, editor.getRoot(keyEntry));
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	 private void onSave() {
		dialog.close();
	}

	public static class EditKeyDialogFxml extends DialogLayoutBase {
		public EditKeyDialogFxml(EditKeyDialog controller, Node body){
			setHeading(label("Edit Key"));
			setBody(body);
			setActions(submit(controller::onSave));
		}
	}
}
