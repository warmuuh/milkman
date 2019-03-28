package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.RequestAspect;

public interface RequestAspectEditor {

	Tab getRoot(RequestAspect aspect);
	
	boolean canHandleAspect(RequestAspect aspect);
	
}
