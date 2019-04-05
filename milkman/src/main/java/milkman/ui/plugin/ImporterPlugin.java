package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;

public interface ImporterPlugin {

	public String getName();
	
	public Node getImportControls();
	
	/**
	 * returns true if success
	 * @param workspace
	 * @param toaster
	 * @return
	 */
	public boolean importInto(Workspace workspace, Toaster toaster);
	
}
