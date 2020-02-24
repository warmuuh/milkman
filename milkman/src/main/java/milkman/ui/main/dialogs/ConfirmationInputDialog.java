package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.submit;

public class ConfirmationInputDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	@FXML Label promptLabel;

	@FXML Label title;

	public ConfirmationInputDialog() {}

	public void showAndWait(String title, String prompt) {

		JFXDialogLayout content = new ConfirmationInputDialogFxml(this);
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


	public static class ConfirmationInputDialogFxml extends JFXDialogLayout {

		public ConfirmationInputDialogFxml(ConfirmationInputDialog controller){
			controller.title = new Label("title");
			setHeading(controller.title);

			controller.promptLabel = new Label();
			setBody(new VBox(controller.promptLabel));

			JFXButton apply = new JFXButton("Apply");
			apply.setDefaultButton(true);
			apply.setOnAction(e -> controller.onSave());

			JFXButton cancel = new JFXButton("Cancel");
			cancel.setCancelButton(true);
			cancel.setOnAction(e -> controller.onCancel());

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}

}
