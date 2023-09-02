package milkman.ui.main.dialogs;

import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.label;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.UUID;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.ui.components.JfxTableEditor;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder.HboxExt;

public class EditEnvironmentDialog {

 	JfxTableEditor<EnvironmentEntry> editor;
	private Dialog dialog;
	Button colorButton;
	FontAwesomeIconView colorIcon;
	private Paint primaryIconFill;

	public void showAndWait(Environment environment) {
		var content = new EditEnvironmentDialogFxml(this);
if (environment.getColor() != null) {
			colorIcon.setFill(Color.web(environment.getColor()));
		}
		colorButton.setOnAction(e -> triggerEditEnvColorDialog(environment, colorIcon));

		editor.enableAddition(() -> new EnvironmentEntry(UUID.randomUUID().toString(), "", "", true));
		editor.addCheckboxColumn("Enabled", EnvironmentEntry::isEnabled, EnvironmentEntry::setEnabled);
		editor.addColumn("name", EnvironmentEntry::getName, EnvironmentEntry::setName);
		editor.addColumn("value", EnvironmentEntry::getValue, EnvironmentEntry::setValue);
		editor.addDeleteColumn("delete");
		editor.setItems(environment.getEntries(), (a,b) -> a.getName().compareTo(b.getName()));
		editor.setRowToStringConverter(e -> e.getName() + ": " + e.getValue());

		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

private void triggerEditEnvColorDialog(Environment environment, FontAwesomeIconView icon) {
		EditEnvironmentColorDialog envDialog = new EditEnvironmentColorDialog();
		envDialog.showAndWait(environment);
		if (!envDialog.isCancelled()) {
			environment.setColor(envDialog.shouldUseColor() ? envDialog.getColor() : null);
			applyEnvColorToIcon(environment, icon);
		}
	}
	private void applyEnvColorToIcon(Environment environment, FontAwesomeIconView icon) {
		if (environment.getColor() != null) {
			icon.setFill(Color.web(environment.getColor()));
		} else {
			icon.setFill(primaryIconFill);
		}
	}

	 public void onClose() {
		dialog.close();
	}


	public static class EditEnvironmentDialogFxml extends DialogLayoutBase {

		public EditEnvironmentDialogFxml(EditEnvironmentDialog controller){

			Button colorButton = controller.colorButton = milkman.utils.fxml.facade.FxmlBuilder.button();
			FontAwesomeIconView icon = controller.colorIcon = new FontAwesomeIconView(FontAwesomeIcon.PAINT_BRUSH, "1.5em");
			controller.primaryIconFill = icon.getFill();
			colorButton.setGraphic(icon);

			setHeading(new HboxExt(label("Edit Environment"), colorButton));

			var editor = controller.editor = new JfxTableEditor<>("env.list");
			editor.setMinHeight(500);
			editor.setMinWidth(800);
			setBody(editor);

			Button close = cancel(controller::onClose, "Close");
			close.getStyleClass().add("dialog-accept");
			setActions(close);
		}

	}



}
