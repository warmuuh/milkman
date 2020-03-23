package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import milkman.utils.VersionLoader;
import milkman.utils.fxml.FxmlUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static milkman.utils.fxml.FxmlBuilder.*;

public class AboutDialog {

	private Dialog dialog;

	private Hyperlink link;

	public void showAndWait() {
		JFXDialogLayout content = new AboutDialogDialogFxml(this);
		link.setOnMouseClicked(e -> {
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/warmuuh/milkman"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
		});

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
