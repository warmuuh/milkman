package milkman.plugin.sync.git;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.SyncDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitSyncDetails extends SyncDetails {

	private String gitUrl;
	private String username;
	private String passwordOrToken;
	
}
