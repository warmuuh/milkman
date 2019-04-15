package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.SyncDetails;
import milkman.domain.Workspace;

public interface WorkspaceSynchronizer {

	
	boolean supportSyncOf(Workspace workspace);
	
	void synchronize(Workspace workspace);

	

	public SynchronizationDetailFactory getDetailFactory();
	
	public interface SynchronizationDetailFactory {
		String getName();
		Node getSyncDetailsControls();
		SyncDetails createSyncDetails();
	}
	
}
