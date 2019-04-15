package milkman.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.ui.main.sync.NoSyncDetails;

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
	
	private SyncDetails syncDetails = new NoSyncDetails();
	
	@JsonIgnore
	private Map<RequestContainer, ResponseContainer> cachedResponses = new HashMap<RequestContainer, ResponseContainer>();
	
	@JsonIgnore
	private List<String> enqueuedRequestIds = new LinkedList<String>();
	
	public Workspace(long id, String workspaceId, String name, List<Collection> collections,
			List<RequestContainer> openRequests, RequestContainer activeRequest) {
		super();
		this.id = id;
		this.workspaceId = workspaceId;
		this.name = name;
		this.collections = collections;
		this.openRequests = openRequests;
		this.activeRequest = activeRequest;
	}
	
	
	
}
 