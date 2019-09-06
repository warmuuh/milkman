package milkmancli.cmds;

import static milkmancli.utils.StringUtil.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class ChangeCollection extends TerminalCommand {

	private final CliContext context;

	public Void call() throws IOException {
		var colName = getParameterValue("collection");

		if (context.getCurrentWorkspace() == null) {
			 throw new IllegalArgumentException("Workspace has to be selected first");
		}
		
		if (context.getCurrentCollection() != null 
				&& stringToId(context.getCurrentCollection().getName()).equals(colName))
			return null;
		
		
		context.setCurrentCollection(colName);
		return null;
	}


	@Override
	public String getName() {
		return "change-collection";
	}

	@Override
	public String getAlias() {
		return "col";
	}

	@Override
	public String getDescription() {
		return "Switched currently activated collection";
	}


	@Override
	public List<Parameter> createParameters() {
		return List.of(new Parameter("collection", "the name of the collection to switch to", new Completion() {
			@Override
			public Collection<String> getCompletionCandidates() {
				return context.getAvailableCollectionNames();
			}
		}));
	}

}