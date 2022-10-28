package milkman.ui.plugin;

import java.util.List;
import milkman.domain.KeySet;

/**
 * 
 * @author pmucha
 *
 */
public interface RequestTypePluginAware {

	void setRequestTypePlugins(List<RequestTypePlugin> plugins);
}
