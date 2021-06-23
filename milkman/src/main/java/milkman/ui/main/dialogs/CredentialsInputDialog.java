package milkman.ui.main.dialogs;

import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.FxmlBuilder.*;
import milkman.utils.fxml.facade.ValidatablePaswordField;
import milkman.utils.fxml.facade.ValidatableTextField;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

public class CredentialsInputDialog {

  private Dialog dialog;
  @Getter
  boolean cancelled = true;

  ValidatableTextField username;
  ValidatablePaswordField password;
  Label title;

  public CredentialsInputDialog() {
  }

  public void showAndWait(String title) {
    var content = new CredentialsInputDialogFxml(this);
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

  private void onSave() {
    if (username.validate() && password.validate()) {
      cancelled = false;
      dialog.close();
    }
  }

  private void onCancel() {
    cancelled = true;
    dialog.close();
  }

  public static class CredentialsInputDialogFxml extends DialogLayoutBase {

    public CredentialsInputDialogFxml(CredentialsInputDialog controller) {
      controller.title = new Label("Title");
      setHeading(controller.title);

      VboxExt vbox = new VboxExt();
      setBody(vbox);

      vbox.add(label("Username"));
      controller.username = vbox.add(vtext());
      controller.username.setValidators(requiredValidator());

      vbox.add(label("Password"));
      controller.password = vbox.add(vpassword());
      controller.password.setValidators(requiredValidator());

      setActions(submit(controller::onSave), cancel(controller::onCancel));

    }

  }

}
