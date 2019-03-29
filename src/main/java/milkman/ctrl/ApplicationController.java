package milkman.ctrl;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jws.Oneway;

import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.persistence.PersistenceManager;
import milkman.ui.commands.AppCommand;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.main.RequestComponent;
import milkman.ui.main.WorkingAreaComponent;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class ApplicationController {

	private final PersistenceManager persistence;
	private final WorkspaceController workspaceController;
	private final UiPluginManager plugins;
	
	public void initApplication() {
		List<String> names = persistence.loadWorkspaceNames();
		if (names.isEmpty())
			createFreshWorkspace();
		else
			loadWorkspace(names.get(0));
				
	}


	private void loadWorkspace(String name) {
		Workspace ws = persistence.loadWorkspaceByName(name)
			.orElseThrow(() -> new IllegalArgumentException("Could not find workspace " + name));
		workspaceController.loadWorkspace(ws);
	}


	private void createFreshWorkspace() {
		RequestTypePlugin requestTypePlugin = plugins.loadRequestTypePlugins().get(0);
		RequestContainer newRequest = requestTypePlugin.createNewRequest();
		plugins.loadRequestAspectPlugins().forEach(p -> p.initializeAspects(newRequest));
		Workspace workspace = new Workspace(0L, 
				UUID.randomUUID().toString(),
				"New Workspace", 
				new LinkedList<>(), 
				io.vavr.collection.List.of(newRequest).toJavaList(), 
				newRequest);
		
		persistence.persistWorkspace(workspace);
		workspaceController.loadWorkspace(workspace);
	}
	
	public void handleCommand(AppCommand command) {
		if (command instanceof AppCommand.PersistWorkspace) {
			persistWorkspace(((AppCommand.PersistWorkspace) command).getWorkspace());
		} else {
			throw new IllegalArgumentException("Unsupported command: " + command);
		}
	}


	private void persistWorkspace(Workspace workspace) {
		persistence.persistWorkspace(workspace);
	}
	
	@PostConstruct
	public void setup() {
		workspaceController.onCommand.add(this::handleCommand);
	}


	public void persistState() {
		persistWorkspace(workspaceController.getActiveWorkspace());
	}
	
}
