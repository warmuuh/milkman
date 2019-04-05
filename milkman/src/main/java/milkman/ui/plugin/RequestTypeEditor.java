package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.RequestContainer;

public interface RequestTypeEditor {

	Node getRoot();
	
	void displayRequest(RequestContainer container);
	
}
