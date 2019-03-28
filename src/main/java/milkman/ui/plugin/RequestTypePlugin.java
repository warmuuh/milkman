package milkman.ui.plugin;

import milkman.domain.RequestContainer;

public interface RequestTypePlugin {
	
	RequestContainer createNewRequest();
	
	MainEditingArea getMainEditingArea();
	
	
}
