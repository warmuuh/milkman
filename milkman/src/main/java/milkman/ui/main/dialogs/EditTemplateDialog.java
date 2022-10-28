package milkman.ui.main.dialogs;

import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.comboBox;
import static milkman.utils.fxml.FxmlBuilder.hbox;
import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.submit;
import static milkman.utils.fxml.FxmlBuilder.text;
import static milkman.utils.fxml.FxmlBuilder.vbox;
import static milkman.utils.fxml.FxmlBuilder.vspace;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.util.List;
import javafx.scene.control.Dialog;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;

public class EditTemplateDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	JFXTextArea template;
	JFXTextField name;
	JFXComboBox requestType;
	private List<String> registeredRequestTypes;

	public EditTemplateDialog(List<String> registeredRequestTypes) {
		this.registeredRequestTypes = registeredRequestTypes;
	}

	public void showAndWait(String templateName, String requestType, String template) {
		JFXDialogLayout content = new EditTemplateDialogFxml(this);
		this.name.setText(templateName);
		this.requestType.getItems().addAll(registeredRequestTypes);
		if (requestType != null) {
			this.requestType.getSelectionModel().select(requestType);
		} else {
			this.requestType.getSelectionModel().selectFirst();
		}

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
		return (String) requestType.getSelectionModel().getSelectedItem();
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
					vspace(5),
					hbox(
						label("Request Type:"),
						controller.requestType = comboBox("tmpl-type")
					),
					vspace(5),
					label("Template:"),
					controller.template = new JFXTextArea()
			));

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
