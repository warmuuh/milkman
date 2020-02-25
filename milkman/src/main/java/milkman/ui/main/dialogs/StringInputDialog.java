package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;

import java.util.Objects;

import static milkman.utils.fxml.FxmlBuilder.*;

public class StringInputDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	@FXML JFXTextField input;
	@FXML Label promptLabel;
	private String prefilledValue;
	@FXML Label title;
	
	public StringInputDialog() {}

	public void showAndWait(String title, String prompt, String prefilledValue) {
		this.prefilledValue = prefilledValue;
		JFXDialogLayout content = new StringInputDialogFxml(this);
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
		if (input.validate()) {
			cancelled = false;
			dialog.close();	
		}
	}

	@FXML private void onCancel() {
		cancelled = true;
		dialog.close();
	}

	public static class StringInputDialogFxml extends JFXDialogLayout {
		public StringInputDialogFxml(StringInputDialog controller){
			setHeading(controller.title = label("Title"));

			var vbox = new FxmlBuilder.VboxExt();
			controller.promptLabel = vbox.add(label(""));
			controller.input = vbox.add(new JFXTextField());
			controller.input.setValidators(requiredValidator());
			setBody(vbox);

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
