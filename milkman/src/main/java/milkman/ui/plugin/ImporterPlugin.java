package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;

/**
* extension point for importers
*/
public interface ImporterPlugin {

    /**
    * name of the importer, will show up in UI
    */
	public String getName();

    /**
    * the controls for setting up the necessary data for import
    */
	public Node getImportControls();

	/**
	 * triggers the import. returns true if success.
	 */
	public boolean importInto(Workspace workspace, Toaster toaster);

}
