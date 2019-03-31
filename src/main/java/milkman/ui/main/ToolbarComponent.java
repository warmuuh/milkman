package milkman.ui.main;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import lombok.Value;
import milkman.ui.commands.AppCommand;
import milkman.utils.Event;

@Singleton
public class ToolbarComponent implements Initializable {

	@FXML ChoiceBox<ChoiceboxEntry> workspaceSelection;

	public final Event<AppCommand> onCommand = new Event<AppCommand>();

	
	
	public interface ChoiceboxEntry {
		public void invoke();
		public boolean isSelectable();
	}
	public static class ChoiceBoxSeparator extends Separator implements ChoiceboxEntry {
		@Override public void invoke() {}
		@Override public boolean isSelectable() {return false;}
	} 
	@Value
	public static class ChoiceboxEntryImpl implements ChoiceboxEntry{
		String name;
		Boolean selectable;
		Runnable action;
		public String toString() {
			return name;
		}
		@Override
		public void invoke() {
			action.run();
		}
		@Override
		public boolean isSelectable() {
			return selectable;
		}
	}
	
	public void setWorkspaces(String activeWs, List<String> workspaceNames) {
		workspaceSelection.getItems().clear();
		ChoiceboxEntryImpl activeEntry = null;
		for(String ws : workspaceNames) {
			ChoiceboxEntryImpl newEntry;
			if (ws.equals(activeWs)) {
				activeEntry = newEntry = new ChoiceboxEntryImpl(ws, true, () -> {});
			} else {
				newEntry = new ChoiceboxEntryImpl(ws, true, () -> onCommand.invoke(new AppCommand.LoadWorkspace(ws)));
			}
			workspaceSelection.getItems().add(newEntry);
		}
		workspaceSelection.getItems().add(new ChoiceBoxSeparator());
		workspaceSelection.getItems().add(new ChoiceboxEntryImpl("Manage Workspaces...", false, () ->  onCommand.invoke(new AppCommand.ManageWorkspaces())));
		workspaceSelection.setValue(activeEntry);
		
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		workspaceSelection.getSelectionModel().selectedItemProperty()
		.addListener((obs, o, n) -> {
			if (n != null) 
				n.invoke();
			
			if (o != null && n != null && !n.isSelectable()) {
				//select old value, if this one is unselectable
				Platform.runLater(() -> workspaceSelection.setValue(o));
			}
		});
		
	}
	
	
	
}
