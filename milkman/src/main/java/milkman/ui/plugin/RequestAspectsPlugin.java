package milkman.ui.plugin;

import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

public interface RequestAspectsPlugin {

	List<RequestAspectEditor> getRequestTabs();
	
	List<ResponseAspectEditor> getResponseTabs();

	/**
	 * will be called to add custom aspects to a container.
	 * 
	 * will be called on creation of requests as well as on displaying requests. 
	 * Second is done because you might drop-in plugins, so existing requests will be enriched on-the-fly.
	 */
	void initializeAspects(RequestContainer request);
	
	/**
	 * will be called to add custom aspects to a container.
	 * will be called on creation of a response.
	 */
	void initializeAspects(ResponseContainer response);
		
}
