package milkmancli.cmds;

import static milkmancli.utils.StringUtil.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkman.domain.Workspace;
import milkman.persistence.PersistenceManager;
import milkmancli.CliContext;
import milkmancli.TerminalCommand;
import milkmancli.utils.StringUtil;

/**
 * Command that clears the screen.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class ChangeWorkspace extends TerminalCommand {

	private final PersistenceManager persistenceManager;
	private final CliContext context;

	public Void call() throws IOException {
		var wsName = getParameterValue("workspace");
		if (wsName == null)
			return null;
		if (context.getCurrentWorkspace() != null 
				&& stringToId(context.getCurrentWorkspace().getName()).equals(wsName))
			return null;
		
		
		
		Workspace ws = findMatching(wsName, persistenceManager.loadWorkspaceNames())
						.flatMap(persistenceManager::loadWorkspaceByName)
						.orElseThrow(()  -> new IllegalArgumentException("Workspace not found " + wsName));
		context.setCurrentWorkspace(ws);
		return null;
	}


	@Override
	public String getName() {
		return "change-workspace";
	}

	@Override
	public String getAlias() {
		return "ws";
	}

	@Override
	public String getDescription() {
		return "Switched currently activated workspace";
	}


	@Override
	public List<Parameter> createParameters() {
		return List.of(new Parameter("workspace", "the name of the workspace to switch to", new Completion() {
			@Override
			public Collection<String> getCompletionCandidates() {
				return persistenceManager.loadWorkspaceNames().stream().map(StringUtil::stringToId).collect(Collectors.toList());
			}
		}));
	}


}