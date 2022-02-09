package milkman.ui.main.dialogs;

import com.jfoenix.controls.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import milkman.domain.Environment;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.AppCommand.CreateNewEnvironment;
import milkman.ui.commands.AppCommand.DeleteEnvironment;
import milkman.ui.commands.AppCommand.RenameEnvironment;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.NoSelectionModel;
import milkman.utils.javafx.JavaFxUtils;

import java.util.List;

import static milkman.utils.fxml.FxmlBuilder.*;

public class ManageEnvironmentsDialog {

	JFXListView<Environment> environmentList;
	private JFXAlert dialog;
	private ObservableList<Environment> environments;

	private Paint primaryIconFill;

	public Event<AppCommand> onCommand = new Event<AppCommand>();
	private List<Environment> originalList;

	
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
			
			JFXToggleNode global = new JFXToggleNode(new FontAwesomeIconView(FontAwesomeIcon.GLOBE, "1.5em"));
			global.setSelected(environment.isGlobal());
			global.selectedProperty().addListener((obs, o, n) -> {
				if (n != null) {
					environment.setGlobal(n);
					if (n)
						environment.setActive(false);
				}
			});

			JFXButton colorButton = new JFXButton();
			FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PAINT_BRUSH, "1.5em");
			primaryIconFill = icon.getFill();
			colorButton.setGraphic(icon);
			if (environment.getColor() != null) {
				icon.setFill(Color.web(environment.getColor()));
			}
			colorButton.setOnAction(e -> triggerEditEnvColorDialog(environment, icon));

			JFXButton deleteButton = new JFXButton();
			deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "1.5em"));
			deleteButton.setOnAction(e -> {
				onCommand.invoke(new DeleteEnvironment(environment));
				refresh();
			});
			
			Label envName = new Label(environment.getName());
			HBox.setHgrow(envName, Priority.ALWAYS);
			return new HBox(envName, renameButton, editButton, global, colorButton, deleteButton);
		}


		private void triggerEditEnvDialog(Environment environment) {
			EditEnvironmentDialog envDialog = new EditEnvironmentDialog();
			envDialog.showAndWait(environment);
		}

		private void triggerEditEnvColorDialog(Environment environment, FontAwesomeIconView icon) {
			EditEnvironmentColorDialog envDialog = new EditEnvironmentColorDialog();
			envDialog.showAndWait(environment);
			if (!envDialog.isCancelled()) {
				environment.setColor(envDialog.shouldUseColor() ? envDialog.getColor() : null);
				applyEnvColorToIcon(environment, icon);
			}
		}

		private void triggerRenameDialog(Environment environment) {
			StringInputDialog inputDialog = new StringInputDialog();
			inputDialog.showAndWait("Rename Environment", "New Name", environment.getName());
			if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
				String newName = inputDialog.getInput();
				onCommand.invoke(new RenameEnvironment(environment, newName));
				refresh();
			}
		}
	}

	private void applyEnvColorToIcon(Environment environment, FontAwesomeIconView icon) {
		if (environment.getColor() != null) {
			icon.setFill(Color.web(environment.getColor()));
		} else {
			icon.setFill(primaryIconFill);
		}
	}

	public void showAndWait(List<Environment> envs) {
		this.originalList = envs;
		JFXDialogLayout content = new ManageEnvironmentsDialogFxml(this);
		JavaFxUtils.publishEscToParent(environmentList);

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
	
	
	
	 public void onClose() {
		dialog.close();
	}


	 public void onCreateEnvironment() {
		StringInputDialog inputDialog = new StringInputDialog();
		inputDialog.showAndWait("Create new Environment", "Name of new Environment", "");
		if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
			String newEnvironmentName = inputDialog.getInput();
			Environment newEnv = new Environment(newEnvironmentName);
			onCommand.invoke(new CreateNewEnvironment(newEnv));
			//hack for first item, otherwise placeholder is not removed for some reason
			environmentList.setItems(FXCollections.observableArrayList());
			Platform.runLater(() -> environmentList.setItems(environments)); 
		}
		
	}

	public static class ManageEnvironmentsDialogFxml extends JFXDialogLayout {
		public ManageEnvironmentsDialogFxml(ManageEnvironmentsDialog controller){

			setHeading(label("Manage Environments"));

			StackPane stackPane = new StackPane();
			var list = controller.environmentList = new JFXListView<>();
			list.setVerticalGap(10.0);
			list.setMinHeight(400);
			stackPane.getChildren().add(list);

			var btn = button(icon(FontAwesomeIcon.PLUS, "1.5em"), controller::onCreateEnvironment);
			btn.getStyleClass().add("btn-add-entry");
			StackPane.setAlignment(btn, Pos.BOTTOM_RIGHT);
			stackPane.getChildren().add(btn);

			setBody(stackPane);

			JFXButton close = cancel(controller::onClose, "Close");
			close.getStyleClass().add("dialog-accept");
			setActions(close);

		}

	}

}
