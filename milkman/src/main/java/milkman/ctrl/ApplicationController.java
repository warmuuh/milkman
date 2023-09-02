package milkman.ctrl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.val;
import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.SyncDetails;
import milkman.domain.Workspace;
import milkman.persistence.OptionEntry;
import milkman.persistence.PersistenceManager;
import milkman.persistence.WorkbenchState;
import milkman.templater.EnvironmentTemplater;
import milkman.templater.PrefixedTemplaterResolver;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.AppCommand.ActivateEnvironment;
import milkman.ui.commands.AppCommand.CreateNewEnvironment;
import milkman.ui.commands.AppCommand.CreateNewWorkspace;
import milkman.ui.commands.AppCommand.DeleteEnvironment;
import milkman.ui.commands.AppCommand.DeleteWorkspace;
import milkman.ui.commands.AppCommand.EditCurrentEnvironment;
import milkman.ui.commands.AppCommand.ExportWorkspace;
import milkman.ui.commands.AppCommand.LoadWorkspace;
import milkman.ui.commands.AppCommand.ManageEnvironments;
import milkman.ui.commands.AppCommand.ManageKeys;
import milkman.ui.commands.AppCommand.ManageOptions;
import milkman.ui.commands.AppCommand.ManageWorkspaces;
import milkman.ui.commands.AppCommand.PersistWorkspace;
import milkman.ui.commands.AppCommand.RenameEnvironment;
import milkman.ui.commands.AppCommand.RenameWorkspace;
import milkman.ui.commands.AppCommand.RequestImport;
import milkman.ui.commands.AppCommand.ShowAbout;
import milkman.ui.commands.AppCommand.SyncWorkspace;
import milkman.ui.commands.AppCommand.ToggleLayout;
import milkman.ui.main.HotkeyManager;
import milkman.ui.main.Toaster;
import milkman.ui.main.ToolbarComponent;
import milkman.ui.main.dialogs.AboutDialog;
import milkman.ui.main.dialogs.CreateWorkspaceDialog;
import milkman.ui.main.dialogs.EditEnvironmentDialog;
import milkman.ui.main.dialogs.ExportDialog;
import milkman.ui.main.dialogs.ImportDialog;
import milkman.ui.main.dialogs.ManageEnvironmentsDialog;
import milkman.ui.main.dialogs.ManageKeysDialog;
import milkman.ui.main.dialogs.ManageWorkspacesDialog;
import milkman.ui.main.dialogs.OptionsDialog;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.main.sync.NoSyncDetails;
import milkman.ui.plugin.Exporter;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.UiPluginManager;
import milkman.update.UpdateChecker;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class ApplicationController {

	private final PersistenceManager persistence;
	private final WorkspaceController workspaceController;
	private final UiPluginManager plugins;
	private final RequestTypeManager requestTypeManager;
	private final SynchManager syncManager;
	private final UpdateChecker updateChecker;
	private final HotkeyManager hotkeyManager;
	private final ToolbarComponent toolbarComponent;
	private final Toaster toaster;

	public void initApplication() {
		WorkbenchState state = persistence.loadWorkbenchState();
		
		List<String> names = persistence.loadWorkspaceNames();
		if (names.isEmpty()) {
			createFreshWorkspace("New Workspace", true);
		}
		else {
			if (names.contains(state.getLoadedWorkspace()))
				loadWorkspace(state.getLoadedWorkspace());
			else
				loadWorkspace(names.get(0));
		}
		
		if (CoreApplicationOptionsProvider.options().isCheckForUpdates())
			updateChecker.checkForUpdateAsync();
	}


	private Workspace loadWorkspace(String name) {
		Workspace ws = persistence.loadWorkspaceByName(name)
			.orElseThrow(() -> new IllegalArgumentException("Could not find workspace " + name));
		workspaceController.loadWorkspace(ws);
		//we have to execute this later because we might have triggerd workspace loading from the toolbar
		Platform.runLater(() -> toolbarComponent.setWorkspaces(ws, persistence.loadWorkspaceNames()));
		return ws;
	}

	private Workspace createFreshWorkspace(String name, boolean loadWorkspace) {
		return createFreshWorkspace(name, new NoSyncDetails(), loadWorkspace);
	}

	private Workspace createFreshWorkspace(String name, SyncDetails syncDetails, boolean loadWorkspace) {
		RequestContainer newRequest = requestTypeManager.createNewRequest(true)
										.orElseThrow(() -> new RuntimeException("No Default Request could be created"));
		Workspace workspace = new Workspace(0L, 
				UUID.randomUUID().toString(),
				name, 
				new LinkedList<>(), 
				new ArrayList<>(List.of(newRequest)), 
				newRequest);
		workspace.setSyncDetails(syncDetails);
		
		//initial pull of shared repo
		if (syncDetails.isSyncActive()) {
			syncWorkspace(() -> {}, true, workspace);
			
		}
		
		persistence.persistWorkspace(workspace);
		if (loadWorkspace)
			workspaceController.loadWorkspace(workspace);
			
		toolbarComponent.setWorkspaces(workspaceController.getActiveWorkspace(), persistence.loadWorkspaceNames());
		
		return workspace;
	}
	
	public void handleCommand(AppCommand command) {
		if (command instanceof PersistWorkspace) {
			persistWorkspace(((PersistWorkspace) command).getWorkspace());
		} else if (command instanceof LoadWorkspace) {
			persistWorkspace(workspaceController.getActiveWorkspace());
			loadWorkspace(((LoadWorkspace) command).getWorkspaceName());
		} else if (command instanceof ManageWorkspaces) {
			openWorkspaceManagementDialog();
		} else if (command instanceof CreateNewWorkspace) {
			Workspace newWorkspace = createNewWorkspace();
			if (newWorkspace != null)
				((CreateNewWorkspace) command).getCallback().accept(newWorkspace);
		} else if (command instanceof DeleteWorkspace) {
			deleteWorkspace(((DeleteWorkspace) command).getWorkspaceName());
		} else if (command instanceof RenameWorkspace) {
			RenameWorkspace renameWorkspace = (RenameWorkspace) command;
			renameWorkspace(renameWorkspace.getWorkspaceName(), renameWorkspace.getNewWorkspaceName());
		} else if (command instanceof ManageEnvironments) {
			openEnvironmentManagementDialog();
		} else if (command instanceof EditCurrentEnvironment) {
			editCurrentEnvironment();
		} else if (command instanceof CreateNewEnvironment) {
			createNewEnvironment(((CreateNewEnvironment) command).getEnv());
		} else if (command instanceof DeleteEnvironment) {
			deleteEnvironment(((DeleteEnvironment) command).getEnv());
		} else if (command instanceof RenameEnvironment) {
			RenameEnvironment renameEnvironment = (RenameEnvironment) command;
			renameEnvironment(renameEnvironment.getEnv(), renameEnvironment.getNewName());
		} else if (command instanceof ActivateEnvironment) {
			activateEnvironment(((ActivateEnvironment) command).getEnv());
		} else if (command instanceof RequestImport) {
			openImportDialog();
		} else if (command instanceof ManageOptions) {
			openOptionsDialog();
		}  else if (command instanceof SyncWorkspace) {
			var syncCommand = (SyncWorkspace) command;
			syncWorkspace(syncCommand.getCallback(), syncCommand.isSyncLocalOnly());
		} else if (command instanceof ExportWorkspace) {
			exportWorkspace(((ExportWorkspace) command).getWorkspaceName());
		} else if (command instanceof ShowAbout) {
			showAboutDialog();
		} else if (command instanceof ManageKeys) {
			showManageKeysDialog();
		} else if (command instanceof ToggleLayout) {
			toggleLayout(((ToggleLayout) command).isHorizontalLayout());
		} else {
			throw new IllegalArgumentException("Unsupported command: " + command);
		}
	}

	private void toggleLayout(boolean horizontalLayout) {
		workspaceController.toggleLayout(horizontalLayout);
	}

	private void showAboutDialog() {
		var aboutDialog = new AboutDialog();
		aboutDialog.showAndWait();
	}

	private void showManageKeysDialog() {
		var keysDialog = new ManageKeysDialog();
		//there is only one keyset for now
		var defaultKeySet = workspaceController.getActiveWorkspace().getActiveKeySet();
		keysDialog.showAndWait(defaultKeySet, plugins.loadKeyEditors());
	}
	private void exportWorkspace(String workspaceName) {
		ExportDialog<Workspace> dialog = new ExportDialog<>();
		List<Exporter<Workspace>> exportPlugins = plugins.loadWorkspaceExportPlugins().stream()
				.map(p -> (Exporter<Workspace>)p)
				.collect(Collectors.toList());

		persistence.loadWorkspaceByName(workspaceName).ifPresentOrElse(
				ws -> dialog.showAndWait(exportPlugins, buildTemplater(), toaster, ws),
				() -> toaster.showToast("Failed to load workspace " + workspaceName)
		);


	}

	private void syncWorkspace(Runnable callback, boolean localSyncOnly) {
		syncWorkspace(callback, localSyncOnly, workspaceController.getActiveWorkspace());
	}

	private void syncWorkspace(Runnable callback, boolean localSyncOnly, Workspace workspace) {
		CompletableFuture<Void> future = syncManager.syncWorkspace(workspace, localSyncOnly);
		toaster.showToast("Sync started");
		future.whenComplete((r, e) -> {
			callback.run();
			if (e != null) {
				toaster.showToast("Sync failed: " + ExceptionUtils.getRootCauseMessage(e));
			} else {
				toaster.showToast("Sync Successfull");
				Platform.runLater(() -> {
					workspaceController.reloadActiveWorkspace();
					toolbarComponent.initEnvironmentDropdown(workspaceController.getActiveWorkspace().getEnvironments());
				});
			}
		});
	}


	private void openOptionsDialog() {
		OptionsDialog dialog = new OptionsDialog();
		List<OptionPageProvider> optionProviders = plugins.loadOptionPages();
		dialog.showAndWait(optionProviders);
		
		persistOptions();
	}


	private void persistOptions() {
		List<OptionPageProvider> optionProviders = plugins.loadOptionPages();
		List<OptionEntry> optEntries = optionProviders.stream()
			.map(p -> new OptionEntry(0L, p.getClass().getName(), p.getOptions()))
			.collect(Collectors.toList());
		persistence.storeOptions(optEntries);
	}

	public void initOptions() {
		List<OptionPageProvider> optionProviders = plugins.loadOptionPages();
		persistence.loadOptions().forEach(e -> {
			optionProviders.stream().filter(p -> p.getClass().getName().equals(e.getOptionProviderClass()))
			.findAny().ifPresent(p -> {
				((OptionPageProvider<OptionsObject>)p).setOptions(e.getOptionsObject());
			});
		});

	}
	
	
	

	private void openImportDialog() {
		ImportDialog dialog = new ImportDialog();
		dialog.showAndWait(plugins.loadImporterPlugins(), toaster, workspaceController.getActiveWorkspace());
		persistWorkspace(workspaceController.getActiveWorkspace());
		workspaceController.loadWorkspace( workspaceController.getActiveWorkspace());
		toolbarComponent.setWorkspaces(workspaceController.getActiveWorkspace(), persistence.loadWorkspaceNames());
		
	}


	private void renameEnvironment(Environment env, String newName) {
		env.setName(newName);
		persistWorkspace(workspaceController.getActiveWorkspace());
		toolbarComponent.initEnvironmentDropdown(workspaceController.getActiveWorkspace().getEnvironments());
	}


	private void deleteEnvironment(Environment env) {
		workspaceController.getActiveWorkspace().getEnvironments().remove(env);
		persistWorkspace(workspaceController.getActiveWorkspace());
		toolbarComponent.initEnvironmentDropdown(workspaceController.getActiveWorkspace().getEnvironments());
	}


	private void activateEnvironment(Optional<Environment> maybeEnv) {
		workspaceController.getActiveWorkspace().getEnvironments().forEach(e -> {
			e.setActive(maybeEnv.map(env -> env == e).orElse(false));
		});
		toolbarComponent.initEnvironmentDropdown(workspaceController.getActiveWorkspace().getEnvironments());
	}


	private void createNewEnvironment(Environment env) {
		workspaceController.getActiveWorkspace().getEnvironments().add(env);
		persistWorkspace(workspaceController.getActiveWorkspace());
		activateEnvironment(Optional.of(env));
	}


	private void openEnvironmentManagementDialog() {
		ManageEnvironmentsDialog dialog = new ManageEnvironmentsDialog();
		dialog.onCommand.add(this::handleCommand);
		dialog.showAndWait(workspaceController.getActiveWorkspace().getEnvironments());
		toolbarComponent.initEnvironmentDropdown(workspaceController.getActiveWorkspace().getEnvironments());
	}



	private void editCurrentEnvironment() {
		Optional<Environment> activeEnv = workspaceController.getActiveWorkspace().getEnvironments().stream()
												.filter(e -> e.isActive() && !e.isGlobal()).findAny();
		activeEnv.ifPresent(environment -> {
			EditEnvironmentDialog envDialog = new EditEnvironmentDialog();
			envDialog.showAndWait(environment);			
		});
		toolbarComponent.initEnvironmentDropdown(workspaceController.getActiveWorkspace().getEnvironments());
	}


	private void renameWorkspace(String workspaceName, String newWorkspaceName) {
		boolean isActiveWorkspace = workspaceController.getActiveWorkspace().getName().equals(workspaceName);
		if (isActiveWorkspace)
			persistWorkspace(workspaceController.getActiveWorkspace());
		
		val ws = persistence.loadWorkspaceByName(workspaceName)
			.orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceName));
		ws.setName(newWorkspaceName);
		persistence.persistWorkspace(ws);
		
		if (isActiveWorkspace) {
			loadWorkspace(newWorkspaceName);
		}
	}


	private void deleteWorkspace(String workspaceName) {
		persistence.deleteWorkspace(workspaceName);
		if (workspaceController.getActiveWorkspace().getName().equals(workspaceName)) {
			List<String> otherWorkspaces = persistence.loadWorkspaceNames();
			if (otherWorkspaces.isEmpty()) {
				createFreshWorkspace("New Workspace", true);
			} else {
				loadWorkspace(otherWorkspaces.get(0));
			}
		}
	}


	private Workspace createNewWorkspace() {
		CreateWorkspaceDialog dialog = new CreateWorkspaceDialog();
		dialog.showAndWait(plugins.loadSyncPlugins(), toaster);
		if (!dialog.wasCancelled()) {
			return createFreshWorkspace(dialog.getWorkspaceName(), dialog.getSyncDetails(), true);
		}
		return null;
	}


	private void openWorkspaceManagementDialog() {
		ManageWorkspacesDialog dialog = new ManageWorkspacesDialog();
		dialog.onCommand.add(this::handleCommand);
		dialog.showAndWait(persistence.loadWorkspaceNames());
	}


	private void persistWorkspace(Workspace workspace) {
		persistence.persistWorkspace(workspace);
	}
	
	@PostConstruct
	public void setup() {
		workspaceController.onCommand.add(this::handleCommand);
		toolbarComponent.onCommand.add(this::handleCommand);
		hotkeyManager.onAppCommand.add(this::handleCommand);
	}


	public void persistState() {
		persistWorkspace(workspaceController.getActiveWorkspace());
		WorkbenchState state = persistence.loadWorkbenchState();
		state.setLoadedWorkspace(workspaceController.getActiveWorkspace().getName());
		persistence.saveWorkbenchState(state);
		persistOptions();
	}


	private Templater buildTemplater() {
		Optional<Environment> activeEnv = workspaceController.getActiveWorkspace().getEnvironments().stream().filter(e -> e.isActive()).findAny();
		List<Environment> globalEnvs = workspaceController.getActiveWorkspace().getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList());
		return new EnvironmentTemplater(activeEnv, globalEnvs, new PrefixedTemplaterResolver(plugins.loadTemplaterPlugins()));
	}

	public void tearDown() {
		persistState();
		workspaceController.tearDown();
	}
}
