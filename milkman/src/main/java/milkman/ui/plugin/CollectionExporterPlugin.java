package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.ui.main.Toaster;

/**
 * extension point for adding collection exporters
 * 
 * @author peter
 *
 */
public interface CollectionExporterPlugin extends Exporter<Collection> {

	
	
}
