package milkman.ui.commands;

import lombok.Value;
import milkman.domain.Workspace;

public interface AppCommand {

	
	@Value
	public static class PersistWorkspace implements AppCommand {
		Workspace workspace;
	}
}
