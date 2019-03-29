package milkman.ctrl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
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
import milkman.utils.ObjectUtils;

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
	
	
	
	public void loadRequestCopy(RequestContainer request) {
		
		//determine if we need to open a new copy of it or if it is opened already:
		val openedReqCopy = activeWorkspace.getOpenRequests().stream()
			.filter(r -> r.getId().equals(request.getId()))
			.findAny();
		
		displayRequest(openedReqCopy.orElseGet(() -> ObjectUtils.deepClone(request)));
	}
	
	public void displayRequest(RequestContainer request) {
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
		displayRequest(request);
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
			saveAsRequest(saveCmd.getRequest());
		} else if (command instanceof UiCommand.SaveRequestCommand) {
			val saveCmd = ((UiCommand.SaveRequestCommand) command);
			saveRequest(saveCmd.getRequest());
		} else if (command instanceof UiCommand.LoadRequest) {
			val openCmd = ((UiCommand.LoadRequest) command);
			loadRequestCopy(openCmd.getRequest());
		} else {
			throw new IllegalArgumentException("Unsupported command");
		}
	}
	
	
	
	private void saveRequest(RequestContainer request) {
		if (StringUtils.isBlank(request.getId())) {
			saveAsRequest(request);
		} else {
			//have to go through all collections bc we dont have a backlink
			//and replace the request
			for (Collection collection : activeWorkspace.getCollections()) {
				for (ListIterator<RequestContainer> iterator = collection.getRequests().listIterator(); iterator.hasNext();) {
					RequestContainer requestContainer = iterator.next();
					if (requestContainer.getId().equals(request.getId())){
						iterator.set(ObjectUtils.deepClone(request));
						return;
					}
				}
			}
		}
	}


	private void saveAsRequest(RequestContainer request) {
		SaveRequestDialog dialog = new SaveRequestDialog(request, activeWorkspace.getCollections());
		dialog.showAndWait();
		if (dialog.isCancelled())
			return;
		
		Optional<Collection> foundCollection = activeWorkspace.getCollections().stream()
				.filter(c -> c.getName().equals(dialog.getCollectionName()))
				.findAny();
		Collection collection = foundCollection.orElseGet(() -> createNewCollection(dialog.getCollectionName()));
		
		request.setId(UUID.randomUUID().toString());
		request.setName(dialog.getRequestName());
		
		collection.getRequests().add(ObjectUtils.deepClone(request));
		loadCollections(activeWorkspace);
		displayRequest(request);
	}

	private Collection createNewCollection(String collectionName) {
		Collection c = new Collection(collectionName, new LinkedList<>());
		activeWorkspace.getCollections().add(c);
		return c;
	}
	

	@PostConstruct
	public void setup() {
		collectionView.onCommand.add(this::handleCommand);
		requestView.onCommand.add(this::handleCommand);
		workingAreaView.onNewRequest.add(x -> createNewRequest());
		workingAreaView.onRequestSelection.add(this::displayRequest);
	}
	
}
