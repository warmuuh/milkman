package milkman.ctrl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.main.WorkingAreaComponent;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class WorkspaceController {

	private final RequestCollectionComponent collectionView;
	private final WorkingAreaComponent requestView;
	
	public void loadWorkspace(Workspace workspace) {
		collectionView.display(workspace.getCollections());
	}
	
	
	public void loadRequest(RequestContainer request) {
		requestView.display(request);
	}
	
	
	
	@PostConstruct
	public void setup() {
		collectionView.onRequestSelection.add(this::loadRequest);
	}
	
}
