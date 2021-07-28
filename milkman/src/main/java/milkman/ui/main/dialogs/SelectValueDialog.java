package milkman.ui.main.dialogs;

import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.BindableComboBox;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder.*;

import java.util.List;
import java.util.Optional;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

public class SelectValueDialog {
	private Dialog dialog;
	@Getter boolean cancelled = true;

	 BindableComboBox<String> valueSelection;
	 Label promptLabel;
	 Label title;
	
	public SelectValueDialog() {}

	public void showAndWait(String title, String prompt, Optional<String> prefilledValue, List<String> values) {
		
		var content = new SelectValueDialogFxml(this);
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

	public static class SelectValueDialogFxml extends DialogLayoutBase {
		public SelectValueDialogFxml(SelectValueDialog controller){
			setHeading(controller.title = label("Title"));

			var vbox = new VboxExt();
			controller.promptLabel = vbox.add(label(""));
			controller.valueSelection = vbox.add(combobox());
			setBody(vbox);

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
