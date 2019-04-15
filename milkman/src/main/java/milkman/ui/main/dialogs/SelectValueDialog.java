package milkman.ui.main.dialogs;

import java.util.List;
import java.util.Optional;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

public class SelectValueDialog {
	private Dialog dialog;
	@Getter boolean cancelled = true;

	@FXML JFXComboBox<String> valueSelection;
	@FXML Label promptLabel;
	@FXML Label title;
	
	public SelectValueDialog() {}

	public void showAndWait(String title, String prompt, Optional<String> prefilledValue, List<String> values) {
		
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/SelectValueDialog.fxml", this);
		this.title.setText(title);
		promptLabel.setText(prompt);
		valueSelection.getItems().addAll(values);
		prefilledValue.ifPresent(valueSelection::setValue);
		
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	public String getInput() {
		return valueSelection.getValue();
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
