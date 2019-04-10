package milkman.ui.plugin;

import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;

public interface ResponseAspectEditor {

	Tab getRoot(RequestContainer request, ResponseContainer response);
	
	boolean canHandleAspect(RequestContainer request, ResponseContainer response);
	
}
