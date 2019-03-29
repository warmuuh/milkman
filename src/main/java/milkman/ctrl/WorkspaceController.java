package milkman.ctrl;

import java.util.LinkedList;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.impl.conn.Wire;

import lombok.RequiredArgsConstructor;
import lombok.val;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.Workspace;
import milkman.ui.commands.UiCommand;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.main.RequestComponent;
import milkman.ui.main.WorkingAreaComponent;
import milkman.ui.main.dialogs.SaveRequestDialog;
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
		loadCollections(workspace);
		workingAreaView.display(workspace.getActiveRequest(), workspace.getOpenRequests());
		
	}


	private void loadCollections(Workspace workspace) {
		collectionView.display(workspace.getCollections());
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
	
	public void handleCommand(UiCommand command) {
		if (command instanceof UiCommand.SubmitRequest) {
			executeRequest(((UiCommand.SubmitRequest) command).getRequest());
		} else if (command instanceof UiCommand.SaveRequestAsCommand) {
			val saveCmd = ((UiCommand.SaveRequestAsCommand) command);
			saveRequest(saveCmd.getRequest());
		} else {
			throw new IllegalArgumentException("Unsupported command");
		}
	}
	
	
	
	private void saveRequest(RequestContainer request) {
		SaveRequestDialog dialog = new SaveRequestDialog(request, activeWorkspace.getCollections());
		dialog.showAndWait();
		if (dialog.isCancelled())
			return;
		
		Optional<Collection> foundCollection = activeWorkspace.getCollections().stream()
				.filter(c -> c.getName().equals(dialog.getCollectionName()))
				.findAny();
		Collection collection = foundCollection.orElseGet(() -> createNewCollection(dialog.getCollectionName()));
		collection.getRequests().add(request);
		loadCollections(activeWorkspace);
	}

	private Collection createNewCollection(String collectionName) {
		Collection c = new Collection(collectionName, new LinkedList<>());
		activeWorkspace.getCollections().add(c);
		return c;
	}
	

	@PostConstruct
	public void setup() {
		collectionView.onRequestSelection.add(this::loadRequest);
		requestView.onCommand.add(this::handleCommand);
		workingAreaView.onNewRequest.add(x -> createNewRequest());
		workingAreaView.onRequestSelection.add(this::loadRequest);
	}
	
}
