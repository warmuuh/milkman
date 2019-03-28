package milkman.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import milkman.ui.main.RequestCollectionComponent;

@Data
public class Workspace {

	private final String name;
	private final List<Collection> collections;
	private final List<RequestContainer> openRequests;
	private final RequestContainer activeRequest;
	private final Map<RequestContainer, ResponseContainer> cachedResponses = new HashMap<RequestContainer, ResponseContainer>();
}
