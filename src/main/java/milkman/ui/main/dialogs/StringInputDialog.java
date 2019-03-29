package milkman.ui.main.dialogs;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.utils.fxml.FxmlUtil;
import javafx.scene.control.Label;

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
