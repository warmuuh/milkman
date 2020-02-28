package milkman.ui.main.sync;

import lombok.Data;
import milkman.domain.SyncDetails;

@Data
public class NoSyncDetails extends SyncDetails {

	private boolean syncActive = false;

}
