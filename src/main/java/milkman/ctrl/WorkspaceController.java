package milkman.ctrl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.Workspace;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.main.RequestComponent;
import milkman.ui.main.WorkingAreaComponent;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class WorkspaceController {

	private final RequestCollectionComponent collectionView;
	private final WorkingAreaComponent workingAreaView;
	private final RequestComponent requestView;
	
	private final UiPluginManager plugins;
	
	public void loadWorkspace(Workspace workspace) {
		collectionView.display(workspace.getCollections());
	}
	
	
	public void loadRequest(RequestContainer request) {
		workingAreaView.display(request);
	}
	
	
	public void executeRequest(RequestContainer request) {
		System.out.println("Executing request: " + request);
		
		RequestTypePlugin plugin = plugins.loadRequestTypePlugins().get(0);
		ResponseContainer response = plugin.executeRequest(request);
		
		plugins.loadRequestAspectPlugins().forEach(a -> a.initializeAspects(response));
		
		workingAreaView.display(response);
	}
	
	
	@PostConstruct
	public void setup() {
		collectionView.onRequestSelection.add(this::loadRequest);
		requestView.onRequestSubmit.add(this::executeRequest);
	}
	
}
