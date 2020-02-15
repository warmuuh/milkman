package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

public class ConfirmationInputDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	@FXML Label promptLabel;

	@FXML Label title;

	public ConfirmationInputDialog() {}

	public void showAndWait(String title, String prompt) {

		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/ConfirmationInputDialog.fxml", this);
		this.title.setText(title);
		promptLabel.setText(prompt);

		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}


	@FXML private void onSave() {
		cancelled = false;
		dialog.close();
	}

	@FXML private void onCancel() {
		cancelled = true;
		dialog.close();
	}

}
