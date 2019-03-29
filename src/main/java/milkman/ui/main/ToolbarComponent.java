package milkman.ui.main;

import java.util.List;

import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import milkman.ui.commands.AppCommand;
import milkman.utils.Event;

@Singleton
public class ToolbarComponent {

	@FXML ChoiceBox workspaceSelection;

	public final Event<AppCommand> onCommand = new Event<AppCommand>();

	
	public void setWorkspaces(String activeWs, List<String> workspaceNames) {
		workspaceSelection.getItems().clear();
		workspaceSelection.getItems().addAll(workspaceNames);
		workspaceSelection.getItems().add(new Separator());
		workspaceSelection.getItems().add("Manage Workspaces...");
		workspaceSelection.setValue(activeWs);
	}
	
	
	
}
