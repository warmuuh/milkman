package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.ResponseAspect;

public interface ResponseAspectEditor {

	Tab getRoot(ResponseAspect aspect);
	
	boolean canHandleAspect(ResponseAspect aspect);
	
}
