package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;

public interface RequestAspectEditor {

	Tab getRoot(RequestContainer request);
	
	boolean canHandleAspect(RequestContainer request);
	
}
