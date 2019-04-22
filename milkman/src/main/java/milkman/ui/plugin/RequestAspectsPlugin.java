package milkman.ui.plugin;

import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;

/**
* extension point for adding aspects to a request.
*/
public interface RequestAspectsPlugin {

    /**
    * returns a list of RequestAspectEditors that this plugin provides
    */
	List<RequestAspectEditor> getRequestTabs();

    /**
    * returns a list of ResponseAspectEditor that this plugin provides
    */
	List<ResponseAspectEditor> getResponseTabs();

	/**
	 * will be called to add custom aspects to a container.
	 *
	 * will be called on creation of requests as well as on displaying requests.
	 * Second is done because you might drop-in plugins, so existing requests will be enriched on-the-fly.
     * Therefore you have to make sure that aspects are only added if they are not yet existing.
	 */
	void initializeRequestAspects(RequestContainer request);

	/**
	 * will be called to add custom aspects to a container.
	 * will be called on creation of a response.
	 */
	void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context);

}
