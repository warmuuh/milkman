package milkman.ui.main.sync;

import milkman.domain.SyncDetails;

public class NoSyncDetails extends SyncDetails {

	@Override
	public boolean isSyncActive() {
		return false;
	}
	
}
