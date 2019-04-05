package milkman.ui.plugin;

import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

public interface RequestTypePlugin {
	
	RequestContainer createNewRequest();
	
	RequestTypeEditor getRequestEditor();
	
	ResponseContainer executeRequest(RequestContainer request, Templater templater);
	
	
}
