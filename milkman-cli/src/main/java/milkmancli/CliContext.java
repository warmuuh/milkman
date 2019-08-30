package milkmancli;

import javax.inject.Singleton;

import lombok.Data;
import milkman.domain.Collection;
import milkman.domain.Workspace;

@Singleton
@Data
public class CliContext {

	Workspace currentWorkspace;
	Collection currentCollection;
}
