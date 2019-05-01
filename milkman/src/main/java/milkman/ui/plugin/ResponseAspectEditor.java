package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;

/**
* an Editor that provides element to edit a specific Aspect of a response.
* the tab will show up, of the request can actually handle the request provided.
*/
public interface ResponseAspectEditor {

    /**
    * returns the tab wich contains the elements to present some information
    * about the response aspect
    */
	Tab getRoot(RequestContainer request, ResponseContainer response);

    /**
    * checks if the given response can be handled. The according request is supplied as well
    */
	boolean canHandleAspect(RequestContainer request, ResponseContainer response);

}
