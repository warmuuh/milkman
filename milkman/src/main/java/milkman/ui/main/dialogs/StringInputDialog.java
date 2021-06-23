package milkman.ui.main.dialogs;

import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder.*;
import milkman.utils.fxml.facade.ValidatableTextField;

import java.util.Objects;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

public class StringInputDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	ValidatableTextField input;
	 Label promptLabel;
	private String prefilledValue;
	 Label title;
	
	public StringInputDialog() {}

	public void showAndWait(String title, String prompt, String prefilledValue) {
		this.prefilledValue = prefilledValue;
		var content = new StringInputDialogFxml(this);
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
	
	 private void onSave() {
		if (input.validate()) {
			cancelled = false;
			dialog.close();	
		}
	}

	 private void onCancel() {
		cancelled = true;
		dialog.close();
	}

	public static class StringInputDialogFxml extends DialogLayoutBase {
		public StringInputDialogFxml(StringInputDialog controller){
			setHeading(controller.title = label("Title"));

			var vbox = new VboxExt();
			controller.promptLabel = vbox.add(label(""));
			controller.input = vbox.add(vtext());
			controller.input.setValidators(requiredValidator());
			setBody(vbox);

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
