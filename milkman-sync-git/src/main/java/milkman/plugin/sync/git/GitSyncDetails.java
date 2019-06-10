package milkman.plugin.sync.git;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	
	@JsonIgnore
	public boolean isSsh() {
		return  (StringUtils.startsWith(gitUrl, "ssh://") || StringUtils.startsWith(gitUrl, "git@"));
	}
}
