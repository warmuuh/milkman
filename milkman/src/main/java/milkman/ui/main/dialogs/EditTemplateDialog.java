package milkman.ui.main.dialogs;

import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.comboBox;
import static milkman.utils.fxml.facade.FxmlBuilder.hbox;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.space;
import static milkman.utils.fxml.facade.FxmlBuilder.submit;
import static milkman.utils.fxml.facade.FxmlBuilder.text;
import static milkman.utils.fxml.facade.FxmlBuilder.vbox;
import static milkman.utils.fxml.facade.FxmlBuilder.vspace;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import lombok.Getter;
import milkman.PlatformUtil;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder;
import milkman.utils.fxml.facade.ValidatableTextArea;

public class EditTemplateDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	ValidatableTextArea template;
	TextField name;
	ComboBox requestType;
	private List<String> registeredRequestTypes;

	public EditTemplateDialog(List<String> registeredRequestTypes) {
		this.registeredRequestTypes = registeredRequestTypes;
	}

	public void showAndWait(String templateName, String requestType, String template) {
		DialogLayoutBase content = new EditTemplateDialogFxml(this);
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

	public static class EditTemplateDialogFxml extends DialogLayoutBase {
		public EditTemplateDialogFxml(EditTemplateDialog controller){
			Button helpBtn = FxmlBuilder.button();
			helpBtn.setOnAction(e -> PlatformUtil.tryOpenBrowser("https://github.com/warmuuh/milkman/blob/master/docs/features.md#custom-templates"));
			helpBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.QUESTION_CIRCLE, "1.5em"));

			setHeading(
					hbox(
							label("Edit custom template..."),
							space(),
							helpBtn
					)
			);

			setBody(
					vbox(
					controller.name = text("tmpl-name", "Template name", true),
					vspace(5),
					hbox(
						label("Request Type:"),
						controller.requestType = comboBox("tmpl-type")
					),
					vspace(5),
					label("Template:"),
					controller.template = FxmlBuilder.vtextArea()
			));

			setActions(submit(controller::onSave), cancel(controller::onCancel));
		}
	}
}
