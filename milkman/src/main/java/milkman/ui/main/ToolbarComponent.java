package milkman.ui.main;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.scene.control.*;
import lombok.Value;
import lombok.val;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.AppCommand.*;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.utils.Event;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

@Singleton
public class ToolbarComponent {

	 ChoiceBox<ChoiceboxEntry> workspaceSelection;

	public final Event<AppCommand> onCommand = new Event<AppCommand>();

	 ChoiceBox<ChoiceboxEntry> environmentSelection;

	 SplitMenuButton syncBtn;

	
	
	public interface ChoiceboxEntry {
		void invoke();
		boolean isSelectable();
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
	
	public void setWorkspaces(Workspace activeWs, List<String> workspaceNames) {
		initWorkspaceDropdown(activeWs, workspaceNames);
		syncBtn.setVisible(activeWs.getSyncDetails().isSyncActive());
		initEnvironmentDropdown(activeWs.getEnvironments());
		
	}

	public void initEnvironmentDropdown(List<Environment> environments) {
		environmentSelection.getItems().clear();
		
		boolean noActiveEnv = environments.stream().noneMatch(e -> e.isActive());
		
		
		val noEnvEntry = new ChoiceboxEntryImpl("No Environment", true, () -> {
			if (!noActiveEnv)
				onCommand.invoke(new ActivateEnvironment(Optional.empty()));
		});
		environmentSelection.getItems().add(noEnvEntry);

		ChoiceboxEntryImpl activeEntry = null;
		for(Environment env : environments) {
			if (env.isGlobal())
				continue;
			
			ChoiceboxEntryImpl newEntry;
			if (env.isActive()) {
				activeEntry = newEntry = new ChoiceboxEntryImpl(env.getName(), true, () -> {});
			} else {
				newEntry = new ChoiceboxEntryImpl(env.getName(), true, () -> onCommand.invoke(new ActivateEnvironment(Optional.of(env))));
			}
			environmentSelection.getItems().add(newEntry);
		}
		environmentSelection.getItems().add(new ChoiceBoxSeparator());
		environmentSelection.getItems().add(new ChoiceboxEntryImpl("Manage Environments...", false, () ->  onCommand.invoke(new ManageEnvironments())));
		environmentSelection.setValue(activeEntry != null ? activeEntry : noEnvEntry);
	}

	private void initWorkspaceDropdown(Workspace activeWs, List<String> workspaceNames) {
		workspaceSelection.getItems().clear();
		ChoiceboxEntryImpl activeEntry = null;
		for(String ws : workspaceNames) {
			ChoiceboxEntryImpl newEntry;
			if (ws.equals(activeWs.getName())) {
				activeEntry = newEntry = new ChoiceboxEntryImpl(ws, true, () -> {});
			} else {
				newEntry = new ChoiceboxEntryImpl(ws, true, () -> onCommand.invoke(new LoadWorkspace(ws)));
			}
			workspaceSelection.getItems().add(newEntry);
		}
		workspaceSelection.getItems().add(new ChoiceBoxSeparator());
		workspaceSelection.getItems().add(new ChoiceboxEntryImpl("Manage Workspaces...", false, () ->  onCommand.invoke(new ManageWorkspaces())));
		workspaceSelection.setValue(activeEntry);
	}

	public void initialize() {
		workspaceSelection.getSelectionModel().selectedItemProperty()
		.addListener((obs, o, n) -> changed(o,n, workspaceSelection));
		
		environmentSelection.getSelectionModel().selectedItemProperty()
		.addListener((obs, o, n) -> changed(o,n, environmentSelection));
	}

	public void changed(ChoiceboxEntry o, ChoiceboxEntry n, ChoiceBox<ChoiceboxEntry> choiceBox) {
		if (n != null) 
			n.invoke();
		
		if (o != null && n != null && !n.isSelectable()) {
			//select old value, if this one is unselectable
			Platform.runLater(() -> choiceBox.setValue(o));
		}
	}
	
	public void refreshEnvironments() {
		
	}

	 public void onImport() {
		onCommand.invoke(new RequestImport());
	}

	 public void onOptions() {
		onCommand.invoke(new ManageOptions());
	}

	public void onToggleLayout(boolean horizontalLayout) {
		onCommand.invoke(new ToggleLayout(horizontalLayout));
	}

	public void onEditCurEnv() {
		onCommand.invoke(new EditCurrentEnvironment());
	}

	 public void onSync() {
		syncBtn.setDisable(true);
		syncBtn.setText("Syncing...");
		onCommand.invoke(new SyncWorkspace(false, () -> {
			syncBtn.setDisable(false);
			syncBtn.setText("Sync");
		}));
	}

	public void onSyncReadOnly() {
		syncBtn.setDisable(true);
		syncBtn.setText("Syncing...");
		onCommand.invoke(new SyncWorkspace(true, () -> {
			syncBtn.setDisable(false);
			syncBtn.setText("Sync");
		}));
	}



	private void onAbout() {
		onCommand.invoke(new ShowAbout());
	}


	private void onEditKeys() {
		onCommand.invoke(new ManageKeys());
	}


	public static class ToolbarComponentFxml extends ToolBar {
		public ToolbarComponentFxml(ToolbarComponent controller) {
			setId("toolbar");
			getItems().add(new Label("Workspace:"));

			controller.workspaceSelection = choiceBox("workspaceSelection");
			getItems().add(controller.workspaceSelection);

			controller.syncBtn = new SplitMenuButton();
			var syncLocalOnly = new MenuItem("sync local only");
			syncLocalOnly.setOnAction(e -> controller.onSyncReadOnly());
			controller.syncBtn.getItems().add(syncLocalOnly);

			controller.syncBtn.setId("syncButton");
			controller.syncBtn.setText("Sync");
			controller.syncBtn.setOnAction(e -> controller.onSync());
			getItems().add(controller.syncBtn);

			getItems().add( button("Import", controller::onImport));


			getItems().add(new Label("Environment:"));
			
			controller.environmentSelection = choiceBox("environmentSelection");
			getItems().add(controller.environmentSelection);
			getItems().add( button(icon(FontAwesomeIcon.PENCIL), controller::onEditCurEnv));

			getItems().add(space(20));
			getItems().add( button(icon(FontAwesomeIcon.KEY), controller::onEditKeys));

			getItems().add(space());

			var layoutToggle = toggle(icon(FontAwesomeIcon.COLUMNS), controller::onToggleLayout);
			if (CoreApplicationOptionsProvider.options().isHorizontalLayout()) {
				layoutToggle.setSelected(true);
			}
			getItems().add(layoutToggle);
			getItems().add( button(icon(FontAwesomeIcon.WRENCH), controller::onOptions));
			getItems().add( button(icon(FontAwesomeIcon.QUESTION_CIRCLE), controller::onAbout));
			
			controller.initialize();
		}
	}
}
