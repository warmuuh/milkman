package milkman.ui.commands;

import lombok.Value;
import milkman.domain.Environment;
import milkman.domain.Workspace;

import java.util.Optional;
import java.util.function.Consumer;

public interface AppCommand {

	
	@Value
	class PersistWorkspace implements AppCommand {
		Workspace workspace;
	}
	
	@Value
	class ManageWorkspaces implements AppCommand {
		
	}
	@Value
	class LoadWorkspace implements AppCommand {
		String workspaceName;
	}
	
	@Value
	class CreateNewWorkspace implements AppCommand {
		Consumer<Workspace> callback;
	}
	
	@Value
	class DeleteWorkspace implements AppCommand {
		String workspaceName;
	}
	
	@Value
	class RenameWorkspace implements AppCommand {
		String workspaceName;
		String newWorkspaceName;
	}

	@Value
	class ExportWorkspace implements AppCommand {
		String workspaceName;
	}
	
	
	
	@Value
	class ManageEnvironments implements AppCommand {
		
	}
	@Value
	class ActivateEnvironment implements AppCommand {
		Optional<Environment> env;
	}
	@Value
	class RenameEnvironment implements AppCommand {
		Environment env;
		String newName;
	}
	
	@Value
	class DeleteEnvironment implements AppCommand {
		Environment env;
	}

	@Value
	class EditCurrentEnvironment implements AppCommand {
	}
	
	@Value
	class CreateNewEnvironment implements AppCommand {
		Environment env;
	}
	
	@Value
	class RequestImport implements AppCommand {
	}
	

	@Value
	class ManageOptions implements AppCommand {
	}
	
	@Value
	class SyncWorkspace implements AppCommand {
		boolean syncLocalOnly;
		Runnable callback;
	}

	@Value
	class ShowAbout implements AppCommand {
	}

	@Value
	class ManageKeys implements AppCommand {
	}

	@Value
	class ToggleLayout implements AppCommand {
		boolean horizontalLayout;
	}
}
