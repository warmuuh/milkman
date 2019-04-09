package milkman.ui.main.dialogs;

import java.util.Collections;
import java.util.List;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import milkman.domain.Environment;
import milkman.ui.commands.AppCommand;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.NoSelectionModel;

public class ManageEnvironmentsDialog {

	@FXML JFXListView<Environment> environmentList;
	private JFXAlert dialog;
	private ObservableList<Environment> environments;

	public Event<AppCommand> onCommand = new Event<AppCommand>();
	private List<Environment> originalList;
	private Environment globalEnv;

	
	public class EnvironmentCell extends JFXListCell<Environment> {
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
			JFXButton renameButton = new JFXButton();
			renameButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL, "1.5em"));
			renameButton.setOnAction(e -> triggerRenameDialog(environment));
			
			JFXButton editButton = new JFXButton();
			editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.LIST, "1.5em"));
			editButton.setOnAction(e -> triggerEditEnvDialog(environment));
			
			JFXButton deleteButton = new JFXButton();
			deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "1.5em"));
			deleteButton.setOnAction(e -> {
				onCommand.invoke(new AppCommand.DeleteEnvironment(environment));
				refresh();
			});
			Label envName = new Label(environment.getName());
			HBox.setHgrow(envName, Priority.ALWAYS);
			return new HBox(envName, renameButton, editButton, deleteButton);
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
	
	public void showAndWait(List<Environment> envs, Environment globalEnv) {
		this.originalList = envs;
		this.globalEnv = globalEnv;
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/ManageEnvironmentsDialog.fxml", this);
		environments = FXCollections.observableList(envs);
		environmentList.setItems(environments);
		environmentList.setCellFactory(l -> new EnvironmentCell());
		environmentList.setSelectionModel(new NoSelectionModel<Environment>());
		environmentList.setPlaceholder(new Label("No Environments created..."));
		dialog = FxmlUtil.createDialog(content);
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
			//hack for first item, otherwise placeholder is not removed for some reason
			environmentList.setItems(FXCollections.observableArrayList());
			Platform.runLater(() -> environmentList.setItems(environments)); 
		}
		
	}



	@FXML public void onEditGlobals() {
		EditEnvironmentDialog envDialog = new EditEnvironmentDialog();
		envDialog.showAndWait(globalEnv);
	}

}
