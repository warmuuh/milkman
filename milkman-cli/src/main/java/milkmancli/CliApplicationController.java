package milkmancli;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import milkman.persistence.PersistenceManager;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Data
public class CliApplicationController {

	private final PersistenceManager persistence;
	
	private final CliContext context;
	
	@PostConstruct
	public void init() {
		var workbenchState = persistence.loadWorkbenchState();
		persistence.loadWorkspaceByName(workbenchState.getLoadedWorkspace())
			.ifPresent(context::setCurrentWorkspace);
	}
	
}
