package milkman.plugin.test.editor;

import javafx.scene.Node;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.RequestTypeEditor;

import static milkman.utils.fxml.FxmlBuilder.vbox;

public class TestContainerEditor implements RequestTypeEditor {
	@Override
	public Node getRoot() {
		return vbox("test-container");
	}

	@Override
	public void displayRequest(RequestContainer container) {

	}
}
