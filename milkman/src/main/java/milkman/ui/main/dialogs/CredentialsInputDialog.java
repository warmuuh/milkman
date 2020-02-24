package milkman.ui.main.dialogs;

import java.util.Objects;

import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.fxml.FxmlBuilder.*;

public class CredentialsInputDialog {

	private Dialog dialog;
	@Getter boolean cancelled = true;

	@FXML JFXTextField username;
	@FXML JFXPasswordField password;
	@FXML Label title;
	
	public CredentialsInputDialog() {}

	public void showAndWait(String title) {
		JFXDialogLayout content = new CredentialsInputDialogFxml(this);
		this.title.setText(title);
		
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	public String getUsername() {
		return username.getText();
	}
	public String getPassword() {
		return password.getText();
	}
	
	@FXML private void onSave() {
		if (username.validate() && password.validate()) {
			cancelled = false;
			dialog.close();	
		}
	}

	@FXML private void onCancel() {
		cancelled = true;
		dialog.close();
	}


	public static class CredentialsInputDialogFxml extends JFXDialogLayout {

		public CredentialsInputDialogFxml(CredentialsInputDialog controller){
			controller.title = new Label("Title");
			setHeading(controller.title);

			FxmlBuilder.VboxExt vbox = new FxmlBuilder.VboxExt();
			setBody(vbox);

			vbox.add(label("Username"));
			controller.username = vbox.add(new JFXTextField());
			controller.username.setValidators(requiredValidator());

			vbox.add(label("Password"));
			controller.password = vbox.add(new JFXPasswordField());
			controller.password.setValidators(requiredValidator());

			setActions(submit(controller::onSave), cancel(controller::onCancel));

		}

	}

}
