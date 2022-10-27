package milkman.ui.main.dialogs;

import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.submit;
import static milkman.utils.fxml.FxmlBuilder.text;
import static milkman.utils.fxml.FxmlBuilder.vbox;

import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Dialog;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

public class EditTemplateDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	JFXTextArea template;
	JFXTextField name;
	JFXTextField requestType;

	public EditTemplateDialog() {}

	public void showAndWait(String templateName, String requestType, String template) {
		JFXDialogLayout content = new EditTemplateDialogFxml(this);
		this.name.setText(templateName);
		this.requestType.setText(requestType);
		this.template.setText(template);

		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	public String getTemplate() {
		return template.getText();
	}

	public String getName() {
		return name.getText();
	}

	public String getRequestType() {
		return requestType.getText();
	}

	private void onSave() {
		if (template.validate()) {
			cancelled = false;
			dialog.close();	
		}
	}

	 private void onCancel() {
		cancelled = true;
		dialog.close();
	}

	public static class EditTemplateDialogFxml extends JFXDialogLayout {
		public EditTemplateDialogFxml(EditTemplateDialog controller){
			setHeading(label("Edit custom template..."));

			setBody(vbox(
					controller.name = text("tmpl-name", "Template name", true),
					controller.requestType = text("tmpl-type", "Request Type", true),
					controller.template = new JFXTextArea()
			));

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
