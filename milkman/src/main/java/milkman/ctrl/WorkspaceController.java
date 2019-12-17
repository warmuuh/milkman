package milkman.ctrl;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.domain.RequestContainer.UnknownRequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.domain.Workspace;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.EnvironmentTemplater;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.CloseRequest;
import milkman.ui.commands.UiCommand.CloseRequest.CloseType;
import milkman.ui.commands.UiCommand.DeleteRequest;
import milkman.ui.commands.UiCommand.RenameRequest;
import milkman.ui.commands.UiCommand.SubmitCustomCommand;
import milkman.ui.commands.UiCommand.SubmitRequest;
import milkman.ui.main.HotkeyManager;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.main.RequestComponent;
import milkman.ui.main.Toaster;
import milkman.ui.main.WorkingAreaComponent;
import milkman.ui.main.dialogs.ExportDialog;
import milkman.ui.main.dialogs.SaveRequestDialog;
import milkman.ui.main.dialogs.StringInputDialog;
import milkman.ui.plugin.CollectionExporterPlugin;
import milkman.ui.plugin.CustomCommand;
import milkman.ui.plugin.Exporter;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.Event;
import milkman.utils.ObjectUtils;
import milkman.utils.fxml.FxmlUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Slf4j
public class WorkspaceController {

	private final RequestCollectionComponent collectionView;
	private final WorkingAreaComponent workingAreaView;
	private final RequestComponent requestView;
	private final RequestTypeManager requestTypeManager;
	private final HotkeyManager hotkeys;
	private final Toaster toaster;
	
	private final UiPluginManager plugins;
	@Getter private Workspace activeWorkspace;
	public final Event<AppCommand> onCommand = new Event<AppCommand>();
	private RequestExecutor executor;
	
	private List<String> requestDisplayHistory = new LinkedList<>();
	
	public void loadWorkspace(Workspace workspace) {
		this.activeWorkspace = workspace;
		loadCollections(workspace);
		closeUnknownRequestContainers(workspace);
		displayRequest(workspace.getActiveRequest());
	}

	public void reloadActiveWorkspace() {
		loadCollections(activeWorkspace);
		closeUnknownRequestContainers(activeWorkspace);
		//refresh from store, if no changes
		if (!activeWorkspace.getActiveRequest().isDirty() && activeWorkspace.getActiveRequest().isInStorage()) {
			loadRequestCopy(activeWorkspace.getActiveRequest().getId(), true);
		}
	}

	private void closeUnknownRequestContainers(Workspace workspace) {
		
		workspace.getOpenRequests().removeIf(UnknownRequestContainer.class::isInstance);
		
		if (workspace.getOpenRequests().isEmpty()) {
			requestTypeManager.createNewRequest(true)
				.ifPresent(workspace.getOpenRequests()::add);
		}
		
		if (workspace.getActiveRequest() instanceof UnknownRequestContainer) {
			workspace.setActiveRequest(workspace.getOpenRequests().get(0));
		}
			
	}


	private void loadCollections(Workspace workspace) {
		collectionView.display(workspace.getCollections());
	}
	
	
	
	public void loadRequestCopy(String requestId, boolean forceRefresh) {
		
		if (forceRefresh) {
			activeWorkspace.getOpenRequests().removeIf(r -> r.getId().equals(requestId));
		}

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
		addToDisplayHistory(request);
		if (!activeWorkspace.getOpenRequests().contains(request))
			activeWorkspace.getOpenRequests().add(request);
		activeWorkspace.setActiveRequest(request);

		
		plugins.loadRequestAspectPlugins().forEach(p -> p.initializeRequestAspects(request));
		workingAreaView.display(request, activeWorkspace.getOpenRequests());
		
		if (activeWorkspace.getCachedResponses().containsKey(request.getId()))
			workingAreaView.displayResponseFor(request, activeWorkspace.getCachedResponses().get(request.getId()));
		else if (activeWorkspace.getEnqueuedRequestIds().containsKey(request.getId())) {
			RequestExecutor executor = activeWorkspace.getEnqueuedRequestIds().get(request.getId());
			workingAreaView.showSpinner(() -> executor.cancel());
		}
		else
			workingAreaView.clearResponse();
	}

	protected void addToDisplayHistory(RequestContainer request) {
		requestDisplayHistory.remove(request.getId());
		requestDisplayHistory.add(request.getId());
	}
	
	public void createNewRequest(Node quickSelectNode) {
		if (quickSelectNode == null) {
			requestTypeManager.createNewRequest(false)
			.ifPresent(this::displayRequest);
		} else {
			requestTypeManager.createNewRequestQuick(quickSelectNode)
				.thenAccept(newRequest -> newRequest.ifPresent(this::displayRequest));
		}
		
	}
	public void createNewRequestWithDefault() {
		requestTypeManager.createNewRequest(true)
			.ifPresent(this::displayRequest);
	}
	public void executeRequest(RequestContainer request) {
		executeRequest(request, Optional.empty());
	}
	
	public void cancelCurrentRequest() {
		if (executor != null) {
			executor.cancel();
		}
	}

	public void executeRequest(RequestContainer request, Optional<CustomCommand> command) {
		workingAreaView.clearResponse();
		activeWorkspace.getCachedResponses().remove(request.getId());
		RequestTypePlugin plugin = requestTypeManager.getPluginFor(request);
		executor = new RequestExecutor(request, plugin, buildTemplater(), command);
		
		workingAreaView.showSpinner(() -> executor.cancel());
		
		RequestExecutionContext context = new RequestExecutionContext(activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny());
		
		long startTime = System.currentTimeMillis();
		executor.setOnScheduled(e -> activeWorkspace.getEnqueuedRequestIds().put(request.getId(), executor));
		executor.setOnCancelled(e -> {
			workingAreaView.hideSpinner();
			activeWorkspace.getEnqueuedRequestIds().remove(request.getId());
			executor = null;
		});
		
		executor.setOnFailed(e -> {
			workingAreaView.hideSpinner();
			activeWorkspace.getEnqueuedRequestIds().remove(request.getId());
			toaster.showToast(executor.getMessage());
			executor = null;
		});
		executor.setOnSucceeded(e -> {
			var asyncCtrl = executor.getValue();
			activeWorkspace.getEnqueuedRequestIds().remove(request.getId());
			plugins.loadRequestAspectPlugins().forEach(a -> a.initializeResponseAspects(request, asyncCtrl.getResponse(), context));
			activeWorkspace.getCachedResponses().put(request.getId(), asyncCtrl);
			log.info("Received response");
			workingAreaView.displayResponseFor(request, asyncCtrl);	
			executor = null;
		});
		
		executor.start();
	}

	private EnvironmentTemplater buildTemplater() {
		Optional<Environment> activeEnv = activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny();
		List<Environment> globalEnvs = activeWorkspace.getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList());
		EnvironmentTemplater templater = new EnvironmentTemplater(activeEnv, globalEnvs);
		return templater;
	}


	public void handleCommand(UiCommand command) {
		log.info("Handling command: " + command);
		if (command instanceof UiCommand.SubmitRequest) {
			executeRequest(((UiCommand.SubmitRequest) command).getRequest());
		} else if (command instanceof UiCommand.CancelActiveRequest) {
			cancelCurrentRequest();
		} else if (command instanceof UiCommand.SubmitCustomCommand) {
			SubmitCustomCommand customCmd = (UiCommand.SubmitCustomCommand) command;
			executeRequest(customCmd.getRequest(), Optional.of(customCmd.getCommand()));
		}else if (command instanceof UiCommand.SubmitActiveRequest) {
			executeRequest(activeWorkspace.getActiveRequest());
		} else if (command instanceof UiCommand.SaveRequestAsCommand) {
			val saveCmd = ((UiCommand.SaveRequestAsCommand) command);
			saveAsRequest(saveCmd.getRequest());
		} else if (command instanceof UiCommand.SaveRequestCommand) {
			val saveCmd = ((UiCommand.SaveRequestCommand) command);
			saveRequest(saveCmd.getRequest());
		} else if (command instanceof UiCommand.SaveActiveRequest) {
			saveRequest(activeWorkspace.getActiveRequest());
		} else if (command instanceof UiCommand.CloseActiveRequest) {
			closeRequest(activeWorkspace.getActiveRequest(), CloseType.CLOSE_THIS);
		} else if (command instanceof UiCommand.LoadRequest) {
			val openCmd = ((UiCommand.LoadRequest) command);
			loadRequestCopy(openCmd.getRequestId(), false);
		} else if (command instanceof UiCommand.SwitchToRequest) {
			displayRequest(((UiCommand.SwitchToRequest) command).getRequest());
		} else if (command instanceof UiCommand.NewRequest) {
			createNewRequest(((UiCommand.NewRequest) command).getQuickSelectNode());
		} else if (command instanceof UiCommand.CloseRequest) {
			CloseRequest closeRequest = (UiCommand.CloseRequest) command;
			closeRequest(closeRequest.getRequest(), closeRequest.getType());
		} else if (command instanceof UiCommand.RenameRequest) {
			RenameRequest renameRequest = (UiCommand.RenameRequest) command;
			renameRequest(renameRequest.getRequest());
		} else if (command instanceof UiCommand.RenameActiveRequest) {
			renameRequest(activeWorkspace.getActiveRequest());
		} else if (command instanceof UiCommand.DeleteRequest) {
			DeleteRequest deleteRequest = (UiCommand.DeleteRequest) command;
			deleteRequest(deleteRequest.getCollection(), deleteRequest.getRequest());
		} else if (command instanceof UiCommand.DeleteCollection) {
			deleteCollection(((UiCommand.DeleteCollection) command).getCollection());
		} else if (command instanceof UiCommand.AddFolder) {
			var addFolderCmd = (UiCommand.AddFolder) command;
			if (addFolderCmd.getCollection() != null)
				addFolderToCollection(addFolderCmd.getCollection());
			else
				addFolderToFolder(addFolderCmd.getFolder());
		} else if (command instanceof UiCommand.DeleteFolder) {
			var deleteFolder = (UiCommand.DeleteFolder) command;
			deleteFolderFromCollection(deleteFolder.getCollection(), deleteFolder.getFolder());
		} else if (command instanceof UiCommand.RenameCollection) {
			renameCollection(((UiCommand.RenameCollection) command).getCollection());
		} else if (command instanceof UiCommand.ExportRequest) {
			exportRequest(((UiCommand.ExportRequest) command).getRequest());
		} else if (command instanceof UiCommand.ExportCollection) {
			exportCollection(((UiCommand.ExportCollection) command).getCollection());
		} else {
			throw new IllegalArgumentException("Unsupported command");
		}
	}
	
	
	
	private void deleteFolderFromCollection(Collection collection, Folder folder) {
		
		deleteAllRequestsFromFolder(collection, folder);
		
		
		if (collection.getFolders().contains(folder)) {
			collection.getFolders().remove(folder);
			loadCollections(activeWorkspace);
		} else {
			Folder parentFolder = null;
			for (Folder curF : collection.getFolders()) {
				parentFolder = findParentFolder(folder, curF);
				if (parentFolder != null) {
					break;
				}
			}
			if (parentFolder != null) {
				parentFolder.getFolders().remove(folder);
				loadCollections(activeWorkspace);
			}
		}
		
	}

	private void deleteAllRequestsFromFolder(Collection collection, Folder folder) {
		folder.getRequests().forEach(rid -> {
			var request = collection.getRequests().stream().filter(req -> req.getId().equals(rid)).findAny();
			request.ifPresent(req -> {
				closeRequest(req, CloseType.CLOSE_THIS);
				collection.getRequests().remove(req);
			});
		});
		folder.getFolders().forEach(child -> deleteAllRequestsFromFolder(collection, child));
	}

	private Folder findParentFolder(Folder folder, Folder curF) {
		if (curF.getFolders().contains(folder))
			return curF;
		
		for (Folder childF : curF.getFolders()) {
			Folder foundParent = findParentFolder(folder, childF);
			if (foundParent != null)
				return foundParent;
		}
		return null;
	}
	
	
	private void addFolderToFolder(Folder folder) {
		StringInputDialog dialog = new StringInputDialog();
		dialog.showAndWait("Add Folder", "New Folder Name", "");
		
		if (dialog.wasChanged() && !dialog.isCancelled()) {
			Folder newFolder = new Folder(UUID.randomUUID().toString(), dialog.getInput(), new LinkedList<>(), new LinkedList<>());
			folder.getFolders().add(newFolder);
			loadCollections(activeWorkspace);
		}
	}

	private void addFolderToCollection(Collection collection) {
		StringInputDialog dialog = new StringInputDialog();
		dialog.showAndWait("Add Folder", "New Folder Name", "");
		
		if (dialog.wasChanged() && !dialog.isCancelled()) {
			Folder folder = new Folder(UUID.randomUUID().toString(), dialog.getInput(), new LinkedList<>(), new LinkedList<>());
			collection.getFolders().add(folder);
			loadCollections(activeWorkspace);
		}
	}

	private void exportCollection(Collection collection) {
		ExportDialog<Collection> dialog = new ExportDialog<>();
		List<Exporter<Collection>> exportPlugins = plugins.loadCollectionExportPlugins().stream()
				.map(p -> (Exporter<Collection>)p)
				.collect(Collectors.toList());
		
		dialog.showAndWait(exportPlugins, buildTemplater(), toaster, collection);		
	}

	private void exportRequest(RequestContainer request) {
		ExportDialog<RequestContainer> dialog = new ExportDialog<>();
		List<Exporter<RequestContainer>> exportPlugins = plugins.loadRequestExportPlugins().stream()
			.filter(p -> p.canHandle(request))
			.map(p -> (Exporter<RequestContainer>)p)
			.collect(Collectors.toList());
		
		dialog.showAndWait(exportPlugins, buildTemplater(), toaster, request);
	}

	private void deleteCollection(Collection collection) {
		activeWorkspace.getCollections().remove(collection);
		loadCollections(activeWorkspace);
		for(RequestContainer request : collection.getRequests()) {
			Optional<RequestContainer> openRequest = activeWorkspace.getOpenRequests().stream()
					.filter(r -> r.getId().equals(request.getId()))
					.findAny();
				openRequest.ifPresent(r -> closeRequest(r, CloseType.CLOSE_THIS));
		}
	}
	
	private void renameCollection(Collection collection) {
		StringInputDialog dialog = new StringInputDialog();
		dialog.showAndWait("Rename Collection", "New Collection name", collection.getName());
		if (dialog.wasChanged() && !dialog.isCancelled()) {
			collection.setName(dialog.getInput());
			loadCollections(activeWorkspace);
		}
		
	}

	private void deleteRequest(Collection collection, RequestContainer request) {
		collection.getRequests().removeIf(r -> r.getId().equals(request.getId()));
		loadCollections(activeWorkspace);
		
		Optional<RequestContainer> openRequest = activeWorkspace.getOpenRequests().stream()
			.filter(r -> r.getId().equals(request.getId()))
			.findAny();
		
		openRequest.ifPresent(r -> closeRequest(r, CloseType.CLOSE_THIS));
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


	private void closeRequest(RequestContainer request, CloseType type) {
		//for now, force close it without asking to save
		int indexOf = activeWorkspace.getOpenRequests().indexOf(request);
		if (indexOf < 0) {
			return;
		}
		
		switch (type) {
		case CLOSE_ALL:
			activeWorkspace.getOpenRequests().clear();
			activeWorkspace.getCachedResponses().clear();
			break;
		case CLOSE_RIGHT:
			while(activeWorkspace.getOpenRequests().size() > indexOf+1) {
				RequestContainer removed = activeWorkspace.getOpenRequests().remove(activeWorkspace.getOpenRequests().size()-1);
				activeWorkspace.getCachedResponses().remove(removed.getId());
			}
			break;
		case CLOSE_OTHERS:
			List<String> toRemove = activeWorkspace.getOpenRequests().stream()
								.filter(r -> !r.getId().equals(request.getId()))
								.map(RequestContainer::getId)
								.collect(Collectors.toList());
			
			toRemove.forEach(activeWorkspace.getCachedResponses()::remove);
			activeWorkspace.getOpenRequests().removeIf(r -> toRemove.contains(r.getId()));
			break;
		case CLOSE_THIS:
			RequestContainer removed = activeWorkspace.getOpenRequests().remove(indexOf);
			activeWorkspace.getCachedResponses().remove(removed.getId());
			break;
		default:
			break;
		}
		if(activeWorkspace.getOpenRequests().isEmpty()) {
			createNewRequestWithDefault();
		} else {
			RequestContainer newRequestToDisplay = getLastOpenedRequest(indexOf);
			displayRequest(newRequestToDisplay);
		}
	}

	protected RequestContainer getLastOpenedRequest(int indexOfClosedRequest) {
		//pruge all closed requests:
		requestDisplayHistory.retainAll(activeWorkspace.getOpenRequests().stream().map(r -> r.getId()).collect(Collectors.toList()));

		if (requestDisplayHistory.isEmpty())
			return activeWorkspace.getOpenRequests()
					.get(Math.min(indexOfClosedRequest, activeWorkspace.getOpenRequests().size()-1));

		String lastOpenedReqId = requestDisplayHistory.get(requestDisplayHistory.size() -1);
		return activeWorkspace.getOpenRequests().stream().filter(r -> r.getId().equals(lastOpenedReqId)).findAny().orElse(null);
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
						request.setDirty(false);
						iterator.set(ObjectUtils.deepClone(request));
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
		Collection c = new Collection(UUID.randomUUID().toString(), collectionName, false, new LinkedList<>(), new LinkedList<>());
		activeWorkspace.getCollections().add(c);
		return c;
	}

	
	

	@PostConstruct
	public void setup() {
		collectionView.onCommand.add(this::handleCommand);
		requestView.onCommand.add(this::handleCommand);
		workingAreaView.onCommand.add(this::handleCommand);
		hotkeys.onCommand.add(this::handleCommand);
	}
	
}
