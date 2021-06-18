package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

import java.util.Optional;

/**
* editor UI for a specific request aspect
*/
public interface RequestAspectEditor {

    /**
    * returns the tab with UI elements to edit the aspect data
    */
	Tab getRoot(RequestContainer request);

    default Tab getRoot(RequestContainer request, Optional<ResponseContainer> existingResponse) {
        return getRoot(request);
    }

    /*
    * checks, if a requestContainer can be handled by this
    * aspect-editor, e.g. if a specific RequestAspect exists.
    */
	boolean canHandleAspect(RequestContainer request);

}
