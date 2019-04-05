package milkman.ui.main.dialogs;

import java.util.Objects;

import com.jfoenix.controls.JFXDialogLayout;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

public class StringInputDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	@FXML TextField input;
	@FXML Label promptLabel;
	private String prefilledValue;
	@FXML Label title;
	
	public StringInputDialog() {}

	public void showAndWait(String title, String prompt, String prefilledValue) {
		this.prefilledValue = prefilledValue;
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/StringInputDialog.fxml", this);
		this.title.setText(title);
		promptLabel.setText(prompt);
		input.setText(prefilledValue);
		
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	public String getInput() {
		return input.getText();
	}
	
	public boolean wasChanged() {
		return !Objects.equals(prefilledValue, input.getText());
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
