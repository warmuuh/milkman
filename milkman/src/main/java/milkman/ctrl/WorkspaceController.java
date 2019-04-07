package milkman.ctrl;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.Workspace;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.EnvironmentTemplater;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.DeleteRequest;
import milkman.ui.commands.UiCommand.RenameRequest;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.main.RequestComponent;
import milkman.ui.main.Toaster;
import milkman.ui.main.WorkingAreaComponent;
import milkman.ui.main.dialogs.SaveRequestDialog;
import milkman.ui.main.dialogs.StringInputDialog;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.Event;
import milkman.utils.ObjectUtils;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Slf4j
public class WorkspaceController {

	private final RequestCollectionComponent collectionView;
	private final WorkingAreaComponent workingAreaView;
	private final RequestComponent requestView;
	
	private final Toaster toaster;
	
	private final UiPluginManager plugins;
	@Getter private Workspace activeWorkspace;
	public final Event<AppCommand> onCommand = new Event<AppCommand>();
	
	public void loadWorkspace(Workspace workspace) {
		this.activeWorkspace = workspace;
		loadCollections(workspace);
		workingAreaView.display(workspace.getActiveRequest(), workspace.getOpenRequests());
		
	}


	private void loadCollections(Workspace workspace) {
		collectionView.display(workspace.getCollections());
	}
	
	
	
	public void loadRequestCopy(String requestId) {
		
		//determine if we need to open a new copy of it or if it is opened already:
		val openedReqCopy = activeWorkspace.getOpenRequests().stream()
			.filter(r -> r.getId().equals(requestId))
			.findAny();
		
		RequestContainer request = openedReqCopy.orElseGet(() -> ObjectUtils.deepClone(findRequest(requestId)));
		displayRequest(request);
	}
	
	private RequestContainer findRequest(String requestId) {
		return activeWorkspace.getCollections().stream()
		.flatMap(c -> c.getRequests().stream())
		.filter(r -> r.getId().equals(requestId))
		.findAny()
		.orElseThrow(() -> new IllegalArgumentException("Request with ID " + requestId + " not found in active workspace"));
	}
	
	public void displayRequest(RequestContainer request) {
		log.info("Diplaying request: " + request);
		if (!activeWorkspace.getOpenRequests().contains(request))
			activeWorkspace.getOpenRequests().add(request);
		activeWorkspace.setActiveRequest(request);

		workingAreaView.display(request, activeWorkspace.getOpenRequests());
		
		if (activeWorkspace.getCachedResponses().containsKey(request))
			workingAreaView.displayResponseFor(request, activeWorkspace.getCachedResponses().get(request));
		else if (activeWorkspace.getEnqueuedRequestIds().contains(request.getId()))
			workingAreaView.showSpinner();
		else
			workingAreaView.clearResponse();
	}
	
	public void createNewRequest() {
		RequestTypePlugin requestTypePlugin = plugins.loadRequestTypePlugins().get(0);
		RequestContainer request = requestTypePlugin.createNewRequest();
		request.setId(UUID.randomUUID().toString());
		plugins.loadRequestAspectPlugins().forEach(p -> p.initializeAspects(request));
		displayRequest(request);
	}
	
	public void executeRequest(RequestContainer request) {
		workingAreaView.showSpinner();
		
		
		Optional<Environment> activeEnv = activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny();
		RequestTypePlugin plugin = plugins.loadRequestTypePlugins().get(0);
		val executor = new RequestExecutor(request, plugin, new EnvironmentTemplater(activeEnv, activeWorkspace.getGlobalEnvironment()));
		
		long startTime = System.currentTimeMillis();
		executor.setOnScheduled(e -> activeWorkspace.getEnqueuedRequestIds().add(request.getId()));
		
		executor.setOnFailed(e -> {
			workingAreaView.hideSpinner();
			activeWorkspace.getEnqueuedRequestIds().remove(request.getId());
			toaster.showToast(executor.getMessage());
		});
		executor.setOnSucceeded(e -> {
			ResponseContainer response = executor.getValue();
			addResponseTimeInfo(response, System.currentTimeMillis() - startTime);
			activeWorkspace.getEnqueuedRequestIds().remove(request.getId());
			plugins.loadRequestAspectPlugins().forEach(a -> a.initializeAspects(response));
			activeWorkspace.getCachedResponses().put(request, response);
			log.info("Received response");
			workingAreaView.displayResponseFor(request, response);	
		});
		
		executor.start();
	}


	private void addResponseTimeInfo(ResponseContainer response, long responseTime) {
		response.getStatusInformations().put("Time", responseTime + "ms");
	}
	
	public void handleCommand(UiCommand command) {
		log.info("Handling command: " + command);
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
			loadRequestCopy(openCmd.getRequestId());
		} else if (command instanceof UiCommand.SwitchToRequest) {
			displayRequest(((UiCommand.SwitchToRequest) command).getRequest());
		} else if (command instanceof UiCommand.NewRequest) {
			createNewRequest();
		} else if (command instanceof UiCommand.CloseRequest) {
			closeRequest(((UiCommand.CloseRequest) command).getRequest());
		} else if (command instanceof UiCommand.RenameRequest) {
			RenameRequest renameRequest = (UiCommand.RenameRequest) command;
			renameRequest(renameRequest.getRequest());
		} else if (command instanceof UiCommand.DeleteRequest) {
			DeleteRequest deleteRequest = (UiCommand.DeleteRequest) command;
			deleteRequest(deleteRequest.getCollection(), deleteRequest.getRequest());
		} else if (command instanceof UiCommand.DeleteCollection) {
			deleteCollection(((UiCommand.DeleteCollection) command).getCollection());
		} else {
			throw new IllegalArgumentException("Unsupported command");
		}
	}
	
	
	
	private void deleteCollection(Collection collection) {
		activeWorkspace.getCollections().remove(collection);
		loadCollections(activeWorkspace);
		for(RequestContainer request : collection.getRequests()) {
			Optional<RequestContainer> openRequest = activeWorkspace.getOpenRequests().stream()
					.filter(r -> r.getId().equals(request.getId()))
					.findAny();
				openRequest.ifPresent(this::closeRequest);
		}
	}


	private void deleteRequest(Collection collection, RequestContainer request) {
		collection.getRequests().removeIf(r -> r.getId().equals(request.getId()));
		loadCollections(activeWorkspace);
		
		Optional<RequestContainer> openRequest = activeWorkspace.getOpenRequests().stream()
			.filter(r -> r.getId().equals(request.getId()))
			.findAny();
		
		openRequest.ifPresent(this::closeRequest);
	}


	private void renameRequest(RequestContainer request) {
		StringInputDialog inputDialog = new StringInputDialog();
		inputDialog.showAndWait("Rename Request", "New Name", request.getName());
		
		if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
			String newName = inputDialog.getInput();
//			request.setName(inputDialog.getInput());
			
			//replace names in working copies:
			activeWorkspace.getOpenRequests().stream()
				.filter(r -> r.getId().equals(request.getId()))
				.findAny().ifPresent(r -> r.setName(newName));

			//replace names in collections:
			activeWorkspace.getCollections().stream().flatMap(c -> c.getRequests().stream())
					.filter(r -> r.getId().equals(request.getId()))
					.findAny().ifPresent(r -> r.setName(newName));
			
			loadCollections(activeWorkspace);
			
			workingAreaView.display(activeWorkspace.getActiveRequest(), activeWorkspace.getOpenRequests());
			
		}
		
		
		
	}


	private void closeRequest(RequestContainer request) {
		//for now, force close it without asking to save
		int indexOf = activeWorkspace.getOpenRequests().indexOf(request);
		if (indexOf < 0) {
			return;
		}
		activeWorkspace.getOpenRequests().remove(indexOf);
		if(activeWorkspace.getOpenRequests().isEmpty()) {
			createNewRequest();
		} else {
			RequestContainer newRequestToDisplay = activeWorkspace.getOpenRequests()
					.get(Math.min(indexOf, activeWorkspace.getOpenRequests().size()-1));
			displayRequest(newRequestToDisplay);
		}
	}


	private void saveRequest(RequestContainer request) {
		if (!request.isInStorage()) {
			saveAsRequest(request);
		} else {
			//have to go through all collections bc we dont have a backlink
			//and replace the request
			outer: for (Collection collection : activeWorkspace.getCollections()) {
				for (ListIterator<RequestContainer> iterator = collection.getRequests().listIterator(); iterator.hasNext();) {
					RequestContainer requestContainer = iterator.next();
					if (requestContainer.getId().equals(request.getId())){
						iterator.set(ObjectUtils.deepClone(request));
						request.setDirty(false);
						loadCollections(activeWorkspace);
						break outer;
					}
				}
			}
		}
		
		onCommand.invoke(new AppCommand.PersistWorkspace(activeWorkspace));
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
		request.setDirty(false);
		
		request.setInStorage(true);
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
		workingAreaView.onCommand.add(this::handleCommand);
	}
	
}
