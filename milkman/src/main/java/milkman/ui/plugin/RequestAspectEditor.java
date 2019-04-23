package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;

/**
* editor UI for a specific request aspect
*/
public interface RequestAspectEditor {

    /**
    * returns the tab with UI elements to edit the aspect data
    */
	Tab getRoot(RequestContainer request);

    /*
    * checks, if a requestContainer can be handled by this
    * aspect-editor, e.g. if a specific RequestAspect exists.
    */
	boolean canHandleAspect(RequestContainer request);

	
	/**
	 * defines the order between aspect tabs, the higher, the more to the right
	 */
	public default int getOrder() {
		return Integer.MAX_VALUE;
	}
}
