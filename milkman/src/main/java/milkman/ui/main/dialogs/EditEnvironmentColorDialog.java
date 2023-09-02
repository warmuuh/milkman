package milkman.ui.main.dialogs;

import static milkman.utils.fxml.facade.FxmlBuilder.HboxExt;
import static milkman.utils.fxml.facade.FxmlBuilder.VboxExt;
import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.submit;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import lombok.Getter;
import milkman.domain.Environment;
import milkman.utils.ColorUtil;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;

public class EditEnvironmentColorDialog {

	private Dialog dialog;
	private ColorPicker colorPicker;
	private CheckBox useColor;

	@Getter
	boolean cancelled = true;

	public void showAndWait(Environment environment) {
		DialogLayoutBase content = new EditEnvironmentColorDialogFxml(this);

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

	public static class EditEnvironmentColorDialogFxml extends DialogLayoutBase {

		public EditEnvironmentColorDialogFxml(EditEnvironmentColorDialog controller){
			setHeading(label("Edit Environment Color"));

			var useColor = controller.useColor = new CheckBox("Use Color");
			var colorPicker = controller.colorPicker = new ColorPicker();
			colorPicker.setOnMouseClicked(e -> useColor.setSelected(true));
			setBody(new VboxExt(
					label("Set highlighting color of environment"),
					label(""),
					new HboxExt(useColor, label("  "), colorPicker)));

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}

	}

}
