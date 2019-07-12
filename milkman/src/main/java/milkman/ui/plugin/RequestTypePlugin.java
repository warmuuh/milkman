package milkman.ui.plugin;

import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

/**
* extension point for introducing new types of requests
*/
public interface RequestTypePlugin extends Orderable {

    /**
    * instantiates a new request with some defaults
    */
	RequestContainer createNewRequest();

    /**
    * returns the editor for editing the main attributes of the request type
    */
	RequestTypeEditor getRequestEditor();

    /**
    * executes the given requests (taking the templater into account)
    * this will be executed in a javafx service and can be blocking
    */
	ResponseContainer executeRequest(RequestContainer request, Templater templater);

    /**
    * returns a short string that allows for differentiation.
    * will show up in ui, so user can select which type of Request he wants to create.
    */
	String getRequestType();

    /**
    * checks, if a given requestContainer can be handled by this plugin
    */
	boolean canHandle(RequestContainer request);
	
	/**
	 * provides a list of custom commands
	 */
	default List<CustomCommand> getCustomCommands()  {
		return Collections.emptyList();
	}
	
	/**
	 * Executes a custom command
	 */
	default ResponseContainer executeCustomCommand(String commandId, RequestContainer request, Templater templater)   {
		throw new IllegalArgumentException("No Custom commands implemented");
	}
}
