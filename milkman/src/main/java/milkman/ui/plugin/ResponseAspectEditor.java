package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;

public interface ResponseAspectEditor {

	Tab getRoot(ResponseAspect aspect, ResponseContainer response);
	
	boolean canHandleAspect(ResponseAspect aspect);
	
}
