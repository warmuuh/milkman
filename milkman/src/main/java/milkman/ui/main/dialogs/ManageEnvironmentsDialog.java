package milkman.ui.main.dialogs;

import static milkman.utils.fxml.facade.FxmlBuilder.button;
import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.icon;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.list;
import static milkman.utils.fxml.facade.FxmlBuilder.toggle;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
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
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.fxml.facade.SimpleListView;
import milkman.utils.javafx.JavaFxUtils;

public class ManageEnvironmentsDialog {

	SimpleListView<Environment> environmentList;
	private Dialog dialog;
	private ObservableList<Environment> environments;

	private Paint primaryIconFill;

	public Event<AppCommand> onCommand = new Event<AppCommand>();
	private List<Environment> originalList;

	public void showAndWait(List<Environment> envs) {
		this.originalList = envs;
		DialogLayoutBase content = new ManageEnvironmentsDialogFxml(this);
		JavaFxUtils.publishEscToParent(environmentList);

		environments = FXCollections.observableList(envs);
		environmentList.setItems(environments);
//		environmentList.setSelectionModel(new NoSelectionModel<Environment>());
//		environmentList.setPlaceholder(new Label("No Environments created..."));
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	private void refresh() {
//		environmentList.refresh();
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


	private HBox createListActions(Environment environment) {
		Button renameButton = button();
		renameButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL, "1.5em"));
		renameButton.setOnAction(e -> triggerRenameDialog(environment));

		Button editButton = button();
		editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.LIST, "1.5em"));
		editButton.setOnAction(e -> triggerEditEnvDialog(environment));

		ToggleButton global = toggle(new FontAwesomeIconView(FontAwesomeIcon.GLOBE, "1.5em"));
		global.setSelected(environment.isGlobal());
		global.selectedProperty().addListener((obs, o, n) -> {
			if (n != null) {
				environment.setGlobal(n);
				if (n)
					environment.setActive(false);
			}
		});

		Button colorButton = new Button();
		FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PAINT_BRUSH, "1.5em");
		primaryIconFill = icon.getFill();
		colorButton.setGraphic(icon);
		if (environment.getColor() != null) {
			icon.setFill(Color.web(environment.getColor()));
		}
		colorButton.setOnAction(e -> triggerEditEnvColorDialog(environment, icon));



		Button deleteButton = button();
		deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "1.5em"));
		deleteButton.setOnAction(e -> {
			onCommand.invoke(new DeleteEnvironment(environment));
			refresh();
		});


		Label envName = new Label(environment.getName());
		HBox.setHgrow(envName, Priority.ALWAYS);
		return new HBox(envName, renameButton, editButton, global, deleteButton);
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

	private void applyEnvColorToIcon(Environment environment, FontAwesomeIconView icon) {
		if (environment.getColor() != null) {
			icon.setFill(Color.web(environment.getColor()));
		} else {
			icon.setFill(primaryIconFill);
		}
	}

	public static class ManageEnvironmentsDialogFxml extends DialogLayoutBase {
		public ManageEnvironmentsDialogFxml(ManageEnvironmentsDialog controller){

			setHeading(label("Manage Environments"));

			StackPane stackPane = new StackPane();
			var list = controller.environmentList = list(10.0, controller::createListActions);
			list.setPrefWidth(400);
			list.setMinHeight(400);
			stackPane.getChildren().add(list);

			var btn = button(icon(FontAwesomeIcon.PLUS, "1.5em"), controller::onCreateEnvironment);
			btn.getStyleClass().add("btn-add-entry");
			StackPane.setAlignment(btn, Pos.BOTTOM_RIGHT);
			stackPane.getChildren().add(btn);

			setBody(stackPane);

			Button close = cancel(controller::onClose, "Close");
			close.getStyleClass().add("dialog-accept");
			setActions(close);

		}

	}

}
