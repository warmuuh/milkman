package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import lombok.Getter;
import milkman.domain.Environment;
import milkman.utils.ColorUtil;
import milkman.utils.fxml.FxmlBuilder.*;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.fxml.FxmlBuilder.*;

public class EditEnvironmentColorDialog {

	private Dialog dialog;
	private ColorPicker colorPicker;
	private CheckBox useColor;

	@Getter
	boolean cancelled = true;

	public void showAndWait(Environment environment) {
		JFXDialogLayout content = new EditEnvironmentColorDialogFxml(this);

		if (environment.getColor() != null){
			useColor.setSelected(true);
			colorPicker.setValue(Color.web(environment.getColor()));
		}
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}


	private void onSave() {
		cancelled = false;
		dialog.close();
	}

	private void onCancel() {
		cancelled = true;
		dialog.close();
	}

	public String getColor() {
		return ColorUtil.toWeb(colorPicker.getValue());
	}

	public boolean shouldUseColor() {
		return useColor.isSelected();
	}

	public static class EditEnvironmentColorDialogFxml extends JFXDialogLayout {

		public EditEnvironmentColorDialogFxml(EditEnvironmentColorDialog controller){
			setHeading(label("Edit Environment Color"));

			var useColor = controller.useColor = new CheckBox("Use Color");
			var colorPicker = controller.colorPicker = new ColorPicker();
			colorPicker.setOnMouseClicked(e -> useColor.setSelected(true));
			setBody(new VboxExt(label("Set highlighting color of environment"), useColor, colorPicker));

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}

	}

	
	
}
