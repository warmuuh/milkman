package milkman.ui.main.sync;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import milkman.domain.SyncDetails;
import milkman.domain.Workspace;
import milkman.ui.plugin.WorkspaceSynchronizer;

public class NoSyncPlugin implements WorkspaceSynchronizer {

	@Override
	public boolean supportSyncOf(Workspace workspace) {
		return false;
	}

	@Override
	public void synchronize(Workspace workspace) {
		
	}

	@Override
	public SynchronizationDetailFactory getDetailFactory() {
		return new SynchronizationDetailFactory() {
			
			@Override
			public Node getSyncDetailsControls() {
				return new HBox();
			}
			
			@Override
			public String getName() {
				return "No Synchronization";
			}
			
			@Override
			public SyncDetails createSyncDetails() {
				return new NoSyncDetails();
			}
		};
	}

	@Override
	public int getOrder() {
		return 0;
	}

	
	
}
