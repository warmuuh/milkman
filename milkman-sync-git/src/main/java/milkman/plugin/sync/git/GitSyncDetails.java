package milkman.plugin.sync.git;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.SyncDetails;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitSyncDetails extends SyncDetails {

	private String gitUrl;
	private String username;
	private String passwordOrToken;
	private String branch = "master";
	
	@JsonIgnore
	public boolean isSsh() {
		return  (StringUtils.startsWith(gitUrl, "ssh://") || StringUtils.startsWith(gitUrl, "git@"));
	}
}
