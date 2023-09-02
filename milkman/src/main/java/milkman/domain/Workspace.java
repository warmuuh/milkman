package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.ctrl.RequestExecutor;
import milkman.ui.main.sync.NoSyncDetails;
import milkman.utils.AsyncResponseControl;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Indices({
    @Index(value = "workspaceId", type = IndexType.Unique),
    @Index(value = "name", type = IndexType.Unique)
})
public class Workspace {

	@Id
	private long id;
	
	private String workspaceId;
	private String name;
	private List<Collection> collections;
	private List<RequestContainer> openRequests;
	private RequestContainer activeRequest;
	private List<Environment> environments = new LinkedList<Environment>();
	private List<KeySet> keySets = new LinkedList<>(List.of(new KeySet("default")));
	
	private SyncDetails syncDetails = new NoSyncDetails();
	
	@JsonIgnore
	private Map<String, AsyncResponseControl> cachedResponses = new HashMap<String, AsyncResponseControl>();
	
	@JsonIgnore
	private Map<String, RequestExecutor> enqueuedRequestIds = new HashMap<String, RequestExecutor>();
	
	public Workspace(long id, String workspaceId, String name, List<Collection> collections,
			List<RequestContainer> openRequests, RequestContainer activeRequest) {
		this.id = id;
		this.workspaceId = workspaceId;
		this.name = name;
		this.collections = collections;
		this.openRequests = openRequests;
		this.activeRequest = activeRequest;
	}

	@JsonIgnore
    public KeySet getActiveKeySet() {
		return keySets.get(0);
    }
}
 