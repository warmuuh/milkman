package milkman.ui.plugin;

import java.util.List;

/**
* this is an interface that can be used in request- and response-aspect-editors.
* If any of those implement this interface, a list of registered contentTypePlugins will be
* injected before doing anything else with those editors.
*/
public interface ContentTypeAwareEditor {

	void setContentTypePlugins(List<ContentTypePlugin> plugins);
}
