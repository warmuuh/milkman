package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListCell;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import milkman.ui.commands.AppCommand;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.NoSelectionModel;

import java.util.List;

public class ManageWorkspacesDialog {

	@FXML ListView<String> workspaceList;
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
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/ManageWorkspacesDialog.fxml", this);
		workspaces = FXCollections.observableList(workspaceNames);
		workspaceList.setItems(workspaces);
		workspaceList.setCellFactory(l -> new WorkspaceCell());
		workspaceList.setSelectionModel(new NoSelectionModel<String>());
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}
	
	
	
	@FXML public void onClose() {
		dialog.close();
	}


	@FXML public void onCreateWorkspace() {
		onCommand.invoke(new AppCommand.CreateNewWorkspace(ws -> Platform.runLater(() -> {
			workspaces.add(ws.getName());
		})));
	}

}
