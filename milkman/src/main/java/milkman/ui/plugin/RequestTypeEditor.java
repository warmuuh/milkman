package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.RequestContainer;

/**
* UI for editing main attributes of a request.
*/
public interface RequestTypeEditor {

    /* UI elemetns of the editor */
	Node getRoot();

    /**
    * called to bind the elements to the given RequestContainer
    */
	void displayRequest(RequestContainer container);

}
