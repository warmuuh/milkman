package milkman.ui.plugin;

import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

public interface RequestAspectsPlugin {

	List<RequestAspectEditor> getRequestTabs();
	
	List<ResponseAspectEditor> getResponseTabs();

	void initializeAspects(RequestContainer request);
	
	void initializeAspects(ResponseContainer response);
		
}
