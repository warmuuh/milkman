package milkman.ctrl;

import javafx.scene.Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.domain.Collection;
import milkman.domain.*;
import milkman.domain.RequestContainer.UnknownRequestContainer;
import milkman.templater.EnvironmentTemplater;
import milkman.templater.PrefixedTemplaterResolver;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.AppCommand.PersistWorkspace;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.*;
import milkman.ui.commands.UiCommand.CloseRequest.CloseType;
import milkman.ui.components.VariableHighlighter;
import milkman.ui.main.*;
import milkman.ui.main.dialogs.ExportDialog;
import milkman.ui.main.dialogs.SaveRequestDialog;
import milkman.ui.main.dialogs.StringInputDialog;
import milkman.ui.plugin.*;
import milkman.utils.Event;
import milkman.utils.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

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
	
	private final List<String> requestDisplayHistory = new LinkedList<>();
	private VariableHighlighter highlighter;

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
		workingAreaView.showSpinner(() -> executor.cancel());

		scheduleRequestExecution(request, command);
	}

	public void scheduleRequestExecution(RequestContainer request, Optional<CustomCommand> command) {
		RequestTypePlugin plugin = requestTypeManager.getPluginFor(request);
		executor = new RequestExecutor(request, plugin, buildTemplater(), command);

		RequestExecutionContext context = getExecutionCtx();
		try {
			plugins.loadRequestAspectPlugins().forEach(a -> a.beforeRequestExecution(request, context));
		} catch (Throwable t) {
			toaster.showToast("Failed to run pre hook: " + t);
		}

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
			try {
				plugins.loadRequestAspectPlugins().forEach(a -> a.initializeResponseAspects(request, asyncCtrl.getResponse(), context));
			} catch (Throwable ex) {
				toaster.showToast("Failed to run post hook: " + ex);
			}
			activeWorkspace.getCachedResponses().put(request.getId(), asyncCtrl);
			log.info("Received response");
			workingAreaView.displayResponseFor(request, asyncCtrl);
			executor = null;
		});

		executor.start();
	}

	private RequestExecutionContext getExecutionCtx() {
		var activeEnv = activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny();
		var globalEnvs = new LinkedList<>(activeWorkspace.getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList()));
		return new RequestExecutionContext(activeEnv, globalEnvs);
	}

	public Templater buildTemplater() {
		Optional<Environment> activeEnv = activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny();
		List<Environment> globalEnvs = activeWorkspace.getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList());
		return new EnvironmentTemplater(activeEnv, globalEnvs, new PrefixedTemplaterResolver(plugins.loadTemplaterPlugins()));
	}

	public Templater buildTemplater(Environment overrideEnv) {
		Optional<Environment> activeEnv = activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny();
		List<Environment> globalEnvs = activeWorkspace.getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList());

		List<Environment> allEnvs = new LinkedList<>(activeEnv.stream().collect(Collectors.toList()));
		allEnvs.addAll(globalEnvs);

		return new EnvironmentTemplater(Optional.of(overrideEnv), allEnvs, new PrefixedTemplaterResolver(plugins.loadTemplaterPlugins()));
	}

	private VariableResolver buildResolver() {
		Optional<Environment> activeEnv = activeWorkspace.getEnvironments().stream().filter(e -> e.isActive()).findAny();
		List<Environment> globalEnvs = activeWorkspace.getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList());
		VariableResolver resolver = new VariableResolver(activeEnv, globalEnvs, new PrefixedTemplaterResolver(plugins.loadTemplaterPlugins()));
		return resolver;
	}

	public void handleCommand(UiCommand command) {
		log.info("Handling command: " + command);
		if (command instanceof SubmitRequest) {
			executeRequest(((SubmitRequest) command).getRequest());
		} else if (command instanceof CancelActiveRequest) {
			cancelCurrentRequest();
		} else if (command instanceof SubmitCustomCommand) {
			SubmitCustomCommand customCmd = (SubmitCustomCommand) command;
			executeRequest(customCmd.getRequest(), Optional.of(customCmd.getCommand()));
		}else if (command instanceof SubmitActiveRequest) {
			executeRequest(activeWorkspace.getActiveRequest());
		} else if (command instanceof SaveRequestAsCommand) {
			val saveCmd = ((SaveRequestAsCommand) command);
			saveAsRequest(saveCmd.getRequest());
		} else if (command instanceof SaveRequestCommand) {
			val saveCmd = ((SaveRequestCommand) command);
			saveRequest(saveCmd.getRequest());
		} else if (command instanceof SaveActiveRequest) {
			saveRequest(activeWorkspace.getActiveRequest());
		} else if (command instanceof CloseActiveRequest) {
			closeRequest(activeWorkspace.getActiveRequest(), CloseType.CLOSE_THIS);
		} else if (command instanceof DuplicateRequest) {
			duplicateRequest(((DuplicateRequest) command).getRequest());
		} else if (command instanceof LoadRequest) {
			val openCmd = ((LoadRequest) command);
			loadRequestCopy(openCmd.getRequestId(), false);
		} else if (command instanceof SwitchToRequest) {
			displayRequest(((SwitchToRequest) command).getRequest());
		} else if (command instanceof NewRequest) {
			createNewRequest(((NewRequest) command).getQuickSelectNode());
		} else if (command instanceof CloseRequest) {
			CloseRequest closeRequest = (CloseRequest) command;
			closeRequest(closeRequest.getRequest(), closeRequest.getType());
		} else if (command instanceof RenameRequest) {
			RenameRequest renameRequest = (RenameRequest) command;
			renameRequest(renameRequest.getRequest());
		} else if (command instanceof RenameActiveRequest) {
			renameRequest(activeWorkspace.getActiveRequest());
		} else if (command instanceof DeleteRequest) {
			DeleteRequest deleteRequest = (DeleteRequest) command;
			deleteRequest(deleteRequest.getCollection(), deleteRequest.getRequest());
		} else if (command instanceof DeleteCollection) {
			deleteCollection(((DeleteCollection) command).getCollection());
		} else if (command instanceof AddFolder) {
			var addFolderCmd = (AddFolder) command;
			if (addFolderCmd.getCollection() != null)
				addFolderToCollection(addFolderCmd.getCollection());
			else
				addFolderToFolder(addFolderCmd.getFolder());
		} else if (command instanceof DeleteFolder) {
			var deleteFolder = (DeleteFolder) command;
			deleteFolderFromCollection(deleteFolder.getCollection(), deleteFolder.getFolder());
		} else if (command instanceof RenameCollection) {
			renameCollection(((RenameCollection) command).getCollection());
		} else if (command instanceof ExportRequest) {
			exportRequest(((ExportRequest) command).getRequest());
		} else if (command instanceof ExportCollection) {
			exportCollection(((ExportCollection) command).getCollection());
		} else if (command instanceof  HighlightVariables) {
			highlightVariables();
		} else if (command instanceof  CancelHighlight) {
			cancelHightlightedVariables();
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
		
		onCommand.invoke(new PersistWorkspace(activeWorkspace));
	}


	private void saveAsRequest(RequestContainer request) {
		SaveRequestDialog dialog = new SaveRequestDialog(request, activeWorkspace.getCollections());
		dialog.showAndWait();
		if (dialog.isCancelled())
			return;

		request.setId(UUID.randomUUID().toString());
		request.setName(dialog.getRequestName());
		request.setDirty(false);
		request.setInStorage(true);

		var collectionName = dialog.getCollectionName();
		Optional<Collection> foundCollection = activeWorkspace.getCollections().stream()
				.filter(c -> c.getName().equals(collectionName))
				.findAny();
		Collection collection = foundCollection.orElseGet(() -> createNewCollection(collectionName));
		collection.getRequests().add(ObjectUtils.deepClone(request));

		var folderPath = dialog.getFolderPath();
		mkFolders(folderPath, collection)
				.ifPresent(f -> f.getRequests().add(request.getId()));


		loadCollections(activeWorkspace);
		displayRequest(request);
	}

	private void duplicateRequest(RequestContainer request) {
		var duplicate = ObjectUtils.deepClone(request);
		duplicate.setId(UUID.randomUUID().toString());
		duplicate.setName("Copy of " + request.getName());
		request.setDirty(true);
		request.setInStorage(false);

		displayRequest(duplicate);
	}


	private void highlightVariables() {
		cancelHightlightedVariables();
		highlighter = new VariableHighlighter(buildResolver());
		highlighter.highlight(workingAreaView.getRequestArea());
	}

	private void cancelHightlightedVariables() {
		if (highlighter != null){
			highlighter.clear();
		}
	}

	private Optional<Folder> mkFolders(List<String> folderPath, Collection collection) {
		List<String> stack = new LinkedList<>(folderPath);
		List<Folder> folders = collection.getFolders();
		Folder foundFolder = null;
		while(!stack.isEmpty()){
			String curPathSegment = stack.remove(0);
			List<Folder> finalFolders = folders;
			foundFolder =  folders.stream().filter(f -> f.getName().equals(curPathSegment)).findFirst().orElseGet(() -> {
				var newFolder = new Folder();
				newFolder.setId(UUID.randomUUID().toString());
				newFolder.setName(curPathSegment);
				finalFolders.add(newFolder);
				return newFolder;
			});
			folders = foundFolder.getFolders();
		}

		return Optional.ofNullable(foundFolder);
	}

	private Collection createNewCollection(String collectionName) {
		Collection c = new Collection(UUID.randomUUID().toString(), collectionName, false, new LinkedList<>(), new LinkedList<>());
		activeWorkspace.getCollections().add(c);
		return c;
	}

	

	public Optional<RequestContainer> findRequestById(String id) {
		return activeWorkspace.getCollections().stream()
				.flatMap(c -> c.getRequests().stream())
				.filter(r -> r.getId().equals(id))
				.findFirst();
	}


	@PostConstruct
	public void setup() {
		collectionView.onCommand.add(this::handleCommand);
		requestView.onCommand.add(this::handleCommand);
		workingAreaView.onCommand.add(this::handleCommand);
		hotkeys.onCommand.add(this::handleCommand);
	}
	
}
