package milkman.ui.components;

import javax.inject.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.*;

@Singleton
public class ContentEditor {

	@FXML TextArea textArea;

	public void setContent(String body) {
		textArea.setText(body);
	}


}
