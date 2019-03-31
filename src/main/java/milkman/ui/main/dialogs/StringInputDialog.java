package milkman.ui.main.dialogs;

import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

public class StringInputDialog {

	private Stage dialog;
	@Getter boolean cancelled = true;

	@FXML TextField input;
	@FXML Label promptLabel;
	private String prefilledValue;
	
	public StringInputDialog() {}

	public void showAndWait(String title, String prompt, String prefilledValue) {
		this.prefilledValue = prefilledValue;
		Parent content = FxmlUtil.loadAndInitialize("/dialogs/StringInputDialog.fxml", this);
		promptLabel.setText(prompt);
		input.setText(prefilledValue);
		
		dialog = FxmlUtil.createDialog(title, content);
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
