package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.submit;

public class EditKeyDialog {

	private Dialog dialog;
	private KeyEditor<?> editor;


	public EditKeyDialog() {}

	public void showAndWait(KeyEntry keyEntry, KeyEditor editor) {
		this.editor = editor;
		JFXDialogLayout content = new EditKeyDialogFxml(this, editor.getRoot(keyEntry));
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	 private void onSave() {
		dialog.close();
	}

	public static class EditKeyDialogFxml extends JFXDialogLayout {
		public EditKeyDialogFxml(EditKeyDialog controller, Node body){
			setHeading(label("Edit Key"));

			ScrollPane scroll = new ScrollPane(body);
			scroll.setFitToWidth(true);
			scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			scroll.setPadding(Insets.EMPTY);
			double maxH = Screen.getPrimary().getVisualBounds().getHeight() * 0.75;
			scroll.setMaxHeight(maxH);

			setBody(scroll);
			setActions(submit(controller::onSave));
		}
	}
}
