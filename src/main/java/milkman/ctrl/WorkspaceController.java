package milkman.ctrl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.impl.conn.Wire;

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
	private Workspace activeWorkspace;
	
	public void loadWorkspace(Workspace workspace) {
		this.activeWorkspace = workspace;
		collectionView.display(workspace.getCollections());
		workingAreaView.display(workspace.getActiveRequest(), workspace.getOpenRequests());
		
	}
	
	
	public void loadRequest(RequestContainer request) {
		if (!activeWorkspace.getOpenRequests().contains(request))
			activeWorkspace.getOpenRequests().add(request);
		
		workingAreaView.display(request, activeWorkspace.getOpenRequests());
		
		if (activeWorkspace.getCachedResponses().containsKey(request))
			workingAreaView.display(activeWorkspace.getCachedResponses().get(request));
		else
			workingAreaView.clearResponse();
	}
	
	public void createNewRequest() {
		RequestTypePlugin requestTypePlugin = plugins.loadRequestTypePlugins().get(0);
		RequestContainer request = requestTypePlugin.createNewRequest();
		plugins.loadRequestAspectPlugins().forEach(p -> p.initializeAspects(request));
		loadRequest(request);
	}
	
	public void executeRequest(RequestContainer request) {
		System.out.println("Executing request: " + request);
		
		RequestTypePlugin plugin = plugins.loadRequestTypePlugins().get(0);
		ResponseContainer response = plugin.executeRequest(request);
		
		plugins.loadRequestAspectPlugins().forEach(a -> a.initializeAspects(response));
		
		activeWorkspace.getCachedResponses().put(request, response);
		
		workingAreaView.display(response);
	}
	
	
	@PostConstruct
	public void setup() {
		collectionView.onRequestSelection.add(this::loadRequest);
		requestView.onRequestSubmit.add(this::executeRequest);
		workingAreaView.onNewRequest.add(x -> createNewRequest());
		workingAreaView.onRequestSelection.add(this::loadRequest);
	}
	
}
