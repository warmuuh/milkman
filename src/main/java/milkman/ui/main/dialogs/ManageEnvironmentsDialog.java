package milkman.ui.main.dialogs;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import milkman.domain.Environment;
import milkman.ui.commands.AppCommand;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.NoSelectionModel;

public class ManageEnvironmentsDialog {

	@FXML ListView<Environment> environmentList;
	private Stage dialog;
	private ObservableList<Environment> environments;

	public Event<AppCommand> onCommand = new Event<AppCommand>();
	private List<Environment> originalList;

	
	public class EnvironmentCell extends ListCell<Environment> {
		@Override
		protected void updateItem(Environment environment, boolean empty) {
			super.updateItem(environment, empty);
			if (empty || environment == null) {
				setText(null);
				setGraphic(null);
			} else {
				setText(null);
	            setGraphic(createEntry(environment));
			}
		}


		private HBox createEntry(Environment environment) {
			Button renameButton = new Button("rename");
			renameButton.setOnAction(e -> triggerRenameDialog(environment));
			
			Button editButton = new Button("edit");
			editButton.setOnAction(e -> triggerEditEnvDialog(environment));
			
			Button deleteButton = new Button("x");
			deleteButton.setOnAction(e -> {
				onCommand.invoke(new AppCommand.DeleteEnvironment(environment));
				refresh();
			});
			return new HBox(new Label(environment.getName()), renameButton, editButton, deleteButton);
		}


		private void triggerEditEnvDialog(Environment environment) {
			EditEnvironmentDialog envDialog = new EditEnvironmentDialog();
			envDialog.showAndWait(environment);
		}


		private void triggerRenameDialog(Environment environment) {
			StringInputDialog inputDialog = new StringInputDialog();
			inputDialog.showAndWait("Rename Environment", "New Name", environment.getName());
			if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
				String newName = inputDialog.getInput();
				onCommand.invoke(new AppCommand.RenameEnvironment(environment, newName));
				refresh();
			}
		}
	}
	
	public void showAndWait(List<Environment> envs) {
		this.originalList = envs;
		Parent content = FxmlUtil.loadAndInitialize("/dialogs/ManageEnvironmentsDialog.fxml", this);
		environments = FXCollections.observableList(envs);
		environmentList.setItems(environments);
		environmentList.setCellFactory(l -> new EnvironmentCell());
		environmentList.setSelectionModel(new NoSelectionModel<Environment>());
		dialog = FxmlUtil.createDialog("Manage Workspaces", content);
		dialog.showAndWait();
	}
	
	private void refresh() {
		environmentList.refresh();
	}
	
	
	
	@FXML public void onClose() {
		dialog.close();
	}


	@FXML public void onCreateEnvironment() {
		StringInputDialog inputDialog = new StringInputDialog();
		inputDialog.showAndWait("Create new Environment", "Name of new Environment", "");
		if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
			String newEnvironmentName = inputDialog.getInput();
			Environment newEnv = new Environment(newEnvironmentName);
			onCommand.invoke(new AppCommand.CreateNewEnvironment(newEnv));
			environmentList.refresh();
		}
		
	}



	@FXML public void onEditGlobals() {
		
	}

}
