package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import milkman.ui.commands.AppCommand;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.NoSelectionModel;

import java.util.List;

import static milkman.utils.fxml.FxmlBuilder.*;

public class ManageWorkspacesDialog {

	 JFXListView<String> workspaceList;
	private Dialog dialog;
	private ObservableList<String> workspaces;

	public Event<AppCommand> onCommand = new Event<AppCommand>();

	
	public class WorkspaceCell extends JFXListCell<String> {
		@Override
		protected void updateItem(String workspaceName, boolean empty) {
			super.updateItem(workspaceName, empty);
			if (empty || workspaceName == null) {
				setText(null);
				setGraphic(null);
			} else {
				setText(null);
	            setGraphic(createEntry(workspaceName));
			}
		}


		private HBox createEntry(String workspaceName) {
			
			JFXButton renameButton = new JFXButton();
			renameButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL, "1.5em"));
			renameButton.setTooltip(new Tooltip("Rename workspace"));
			renameButton.setOnAction(e -> triggerRenameDialog(workspaceName));
			
			JFXButton deleteButton = new JFXButton();
			deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "1.5em"));
			deleteButton.setTooltip(new Tooltip("Delete workspace"));
			deleteButton.setOnAction(e -> {
				onCommand.invoke(new AppCommand.DeleteWorkspace(workspaceName));
				Platform.runLater(() -> workspaces.remove(workspaceName));
			});

			JFXButton exportButton = new JFXButton();
			exportButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.UPLOAD, "1.5em"));
			exportButton.setTooltip(new Tooltip("Export workspace"));
			exportButton.setOnAction(e -> triggerExportWorkspaceDialog(workspaceName));

			Label wsName = new Label(workspaceName);
			HBox.setHgrow(wsName, Priority.ALWAYS);
			
			return new HBox(wsName, renameButton, deleteButton, exportButton);
		}


		private void triggerExportWorkspaceDialog(String workspaceName){
			onCommand.invoke(new AppCommand.ExportWorkspace(workspaceName));
		}

		private void triggerRenameDialog(String workspaceName) {
			StringInputDialog inputDialog = new StringInputDialog();
			inputDialog.showAndWait("Rename Workspace", "New Name", workspaceName);
			if (!inputDialog.isCancelled() && inputDialog.wasChanged()) {
				String newName = inputDialog.getInput();
				onCommand.invoke(new AppCommand.RenameWorkspace(workspaceName, newName));
				Platform.runLater(() -> {
					int oldIdx = workspaces.indexOf(workspaceName);
					workspaces.set(oldIdx, newName);
				});
			}
		}
	}
	
	public void showAndWait(List<String> workspaceNames) {
		JFXDialogLayout content = new ManageWorkspacesDialogFxml(this);
		workspaces = FXCollections.observableList(workspaceNames);
		workspaceList.setItems(workspaces);
		workspaceList.setCellFactory(l -> new WorkspaceCell());
		workspaceList.setSelectionModel(new NoSelectionModel<String>());
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}
	
	
	
	 public void onClose() {
		dialog.close();
	}


	 public void onCreateWorkspace() {
		onCommand.invoke(new AppCommand.CreateNewWorkspace(ws -> Platform.runLater(() -> {
			workspaces.add(ws.getName());
		})));
	}


	public static class ManageWorkspacesDialogFxml extends JFXDialogLayout {
		public ManageWorkspacesDialogFxml(ManageWorkspacesDialog controller){
			setHeading(label("Workspaces"));

			StackPane stackPane = new StackPane();
			var list = controller.workspaceList = new JFXListView<>();
			list.setVerticalGap(10.0);
			list.setMinHeight(400);
			stackPane.getChildren().add(list);

			var btn = button(icon(FontAwesomeIcon.PLUS, "1.5em"), controller::onCreateWorkspace);
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
