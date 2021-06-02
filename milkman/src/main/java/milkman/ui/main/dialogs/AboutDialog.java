package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import milkman.PlatformUtil;
import milkman.utils.VersionLoader;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.fxml.FxmlBuilder.*;

public class AboutDialog {

	private Dialog dialog;

	private Hyperlink link;

	public void showAndWait() {
		JFXDialogLayout content = new AboutDialogDialogFxml(this);
		link.setOnMouseClicked(e -> PlatformUtil.tryOpenBrowser("https://github.com/warmuuh/milkman"));

		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	private void onClose() {
		dialog.close();
	}

	public static class AboutDialogDialogFxml extends JFXDialogLayout {
		public AboutDialogDialogFxml(AboutDialog controller){
			setHeading(label("About Milkman"));

			var vbox = new VboxExt();
			vbox.setAlignment(Pos.CENTER);
			vbox.add(new ImageView(new Image(getClass().getResourceAsStream("/images/icon.png"), 100, 100, true, true)));
			vbox.add(label("Version " + VersionLoader.loadCurrentVersion()));
			var hyperlink = controller.link = new Hyperlink("http://github.com/warmuuh/milkman");
			vbox.add(hyperlink);
			setBody(vbox);

			setActions(cancel(controller::onClose, "Close"));
		}
	}


}
