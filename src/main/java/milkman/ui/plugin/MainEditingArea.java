package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.RequestContainer;

public interface MainEditingArea {

	Node getRoot();
	
	void displayRequest(RequestContainer container);
	
}
