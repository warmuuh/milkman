package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.RequestContainer;
import milkman.ui.main.Toaster;

/**
 * extension point for adding request exporters
 * 
 * @author peter
 *
 */
public interface RequestExporterPlugin extends Exporter<RequestContainer> {

	/**
	 * returns true if the supplied type of request can be exported by this plugin 
	 */
	public boolean canHandle(RequestContainer request);
	
	
}
