package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;

import java.util.List;
import java.util.Optional;

import static milkman.utils.fxml.FxmlBuilder.*;

public class SelectValueDialog {
	private Dialog dialog;
	@Getter boolean cancelled = true;

	 JFXComboBox<String> valueSelection;
	 Label promptLabel;
	 Label title;
	
	public SelectValueDialog() {}

	public void showAndWait(String title, String prompt, Optional<String> prefilledValue, List<String> values) {
		
		JFXDialogLayout content = new SelectValueDialogFxml(this);
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
	
	 private void onSave() {
		cancelled = false;
		dialog.close();	
	}

	 private void onCancel() {
		cancelled = true;
		dialog.close();
	}

	public static class SelectValueDialogFxml extends JFXDialogLayout {
		public SelectValueDialogFxml(SelectValueDialog controller){
			setHeading(controller.title = label("Title"));

			var vbox = new FxmlBuilder.VboxExt();
			controller.promptLabel = vbox.add(label(""));
			controller.valueSelection = vbox.add(new JFXComboBox<>());
			setBody(vbox);

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
