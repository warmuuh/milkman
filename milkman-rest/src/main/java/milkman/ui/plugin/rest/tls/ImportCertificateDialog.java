package milkman.ui.plugin.rest.tls;

import static milkman.utils.fxml.FxmlBuilder.button;
import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.hbox;
import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.requiredValidator;
import static milkman.utils.fxml.FxmlBuilder.space;
import static milkman.utils.fxml.FxmlBuilder.submit;
import static milkman.utils.fxml.FxmlBuilder.vspace;

import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import lombok.Getter;
import milkman.ui.main.Toaster;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.FxmlUtil;
import org.apache.commons.io.FileUtils;

public class ImportCertificateDialog {

  private final Toaster toaster;
  private Dialog dialog;
  @Getter
  boolean cancelled = true;

  JFXTextField input;
  String base64Certificate = null;
  String base64PrivateKey = null;

  public ImportCertificateDialog(Toaster toaster) {
    this.toaster = toaster;
  }

  public void showAndWait() {
    showAndWait(null);
  }

  public void showAndWait(Certificate existingCertificate) {
    JFXDialogLayout content = new ImportCertificateDialogFxml(this);
    if (existingCertificate != null) {
      input.setText(existingCertificate.getName());
      base64Certificate = existingCertificate.getBase64Certificate();
      base64PrivateKey = existingCertificate.getBase64PrivateKey();
    }

    dialog = FxmlUtil.createDialog(content);
    dialog.showAndWait();
  }

  public Certificate getCertificate() throws IOException {
    return new Certificate(base64Certificate, base64PrivateKey, input.getText(), CertificateType.PEM);
  }

  private void onSave() {
    if (input.validate()) {
      cancelled = false;
      dialog.close();
    }
  }

  private void onCancel() {
    cancelled = true;
    dialog.close();
  }

  private void importPemFile() {
    FileChooser fileChooser = new FileChooser();
    File f = fileChooser.showOpenDialog(FxmlUtil.getPrimaryStage());
    if (f != null && f.exists() && f.isFile()) {
      try {
        base64Certificate = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(f));
      } catch (Exception e) {
        toaster.showToast("Failed to read PEM file");
      }
    }
  }

  private void importPrivateKeyFile() {
    FileChooser fileChooser = new FileChooser();
    File f = fileChooser.showOpenDialog(FxmlUtil.getPrimaryStage());
    if (f != null && f.exists() && f.isFile()) {
      try {
        base64PrivateKey = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(f));
      } catch (Exception e) {
        toaster.showToast("Failed to read Private Key file");
      }
    }
  }
  public static class ImportCertificateDialogFxml extends JFXDialogLayout {

    public ImportCertificateDialogFxml(ImportCertificateDialog controller) {
      setHeading(label("Import Certificate"));

      var vbox = new FxmlBuilder.VboxExt();
      vbox.add(label("Certificate Name"));
      controller.input = vbox.add(new JFXTextField());
      controller.input.setValidators(requiredValidator());

      vbox.add(vspace(20));

      HboxExt hbox = vbox.add(hbox());
      var pemBtn = hbox.add(button("Import PEM file", controller::importPemFile));
      pemBtn.getStyleClass().add("primary-button");

      hbox.add(space(10));

      var keyBtn = hbox.add(button("Import Key file", controller::importPrivateKeyFile));
      keyBtn.getStyleClass().add("primary-button");

      setBody(vbox);
      setActions(submit(controller::onSave), cancel(controller::onCancel));
    }
  }
}
