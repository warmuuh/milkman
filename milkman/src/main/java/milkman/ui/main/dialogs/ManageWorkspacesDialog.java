package milkman.ui.main.dialogs;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.AppCommand.CreateNewWorkspace;
import milkman.ui.commands.AppCommand.DeleteWorkspace;
import milkman.ui.commands.AppCommand.ExportWorkspace;
import milkman.ui.commands.AppCommand.RenameWorkspace;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.NoSelectionModel;
import milkman.utils.fxml.facade.DialogLayoutBase;
import milkman.utils.javafx.JavaFxUtils;

import java.util.List;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

public class ManageWorkspacesDialog {

	ListView<String> workspaceList;
	private Dialog dialog;
	private ObservableList<String> workspaces;

	public Event<AppCommand> onCommand = new Event<AppCommand>();

	public void showAndWait(List<String> workspaceNames) {
		var content = new ManageWorkspacesDialogFxml(this);
		JavaFxUtils.publishEscToParent(workspaceList);

		workspaces = FXCollections.observableList(workspaceNames);
		workspaceList.setItems(workspaces);
		workspaceList.setSelectionModel(new NoSelectionModel<String>());
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	
	 public void onClose() {
		dialog.close();
	}


	 public void onCreateWorkspace() {
		onCommand.invoke(new CreateNewWorkspace(ws -> Platform.runLater(() -> {
			workspaces.add(ws.getName());
		})));
	}

	private HBox createListActions(String workspaceName) {

		Button renameButton = button();
		renameButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL, "1.5em"));
		renameButton.setTooltip(new Tooltip("Rename workspace"));
		renameButton.setOnAction(e -> triggerRenameDialog(workspaceName));

		Button deleteButton = button();
		deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "1.5em"));
		deleteButton.setTooltip(new Tooltip("Delete workspace"));
		deleteButton.setOnAction(e -> {
			onCommand.invoke(new DeleteWorkspace(workspaceName));
			Platform.runLater(() -> workspaces.remove(workspaceName));
		});

		Button exportButton = button();
		exportButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.UPLOAD, "1.5em"));
		exportButton.setTooltip(new Tooltip("Export workspace"));
		exportButton.setOnAction(e -> triggerExportWorkspaceDialog(workspaceName));

		Label wsName = new Label(workspaceName);
		HBox.setHgrow(wsName, Priority.ALWAYS);

		return new HBox(wsName, renameButton, deleteButton, exportButton);
	}


	private void triggerExportWorkspaceDialog(String workspaceName){
		onCommand.invoke(new ExportWorkspace(workspaceName));
	}

	private void triggerRenameDialog(String workspaceName) {
		StringInputDialog inputDialog = new StringInputDialog();
		inputDialog.showAndWait("Rename Workspace", "New Name", workspaceName);
		if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
			String newName = inputDialog.getInput();
			onCommand.invoke(new RenameWorkspace(workspaceName, newName));
			Platform.runLater(() -> {
				int oldIdx = workspaces.indexOf(workspaceName);
				workspaces.set(oldIdx, newName);
			});
		}
	}


	public static class ManageWorkspacesDialogFxml extends DialogLayoutBase {
		public ManageWorkspacesDialogFxml(ManageWorkspacesDialog controller){
			setHeading(label("Workspaces"));

			StackPane stackPane = new StackPane();
			var list = controller.workspaceList = list(10.0, controller::createListActions);
			list.setMinHeight(400);
			stackPane.getChildren().add(list);

			var btn = button(icon(FontAwesomeIcon.PLUS, "1.5em"), controller::onCreateWorkspace);
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
