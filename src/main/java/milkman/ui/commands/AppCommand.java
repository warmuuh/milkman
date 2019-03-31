package milkman.ui.commands;

import lombok.Value;
import milkman.domain.Workspace;

public interface AppCommand {

	
	@Value
	public static class PersistWorkspace implements AppCommand {
		Workspace workspace;
	}
	
	@Value
	public static class ManageWorkspaces implements AppCommand {
		
	}
	@Value
	public static class LoadWorkspace implements AppCommand {
		String workspaceName;
	}
	
	@Value
	public static class CreateNewWorkspace implements AppCommand {
		String newWorkspaceName;
	}
	
	@Value
	public static class DeleteWorkspace implements AppCommand {
		String workspaceName;
	}
	
	@Value
	public static class RenameWorkspace implements AppCommand {
		String workspaceName;
		String newWorkspaceName;
	}
	
	
}
