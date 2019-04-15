package milkman.ui.main.dialogs;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import milkman.domain.SyncDetails;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.WorkspaceSynchronizer;
import milkman.ui.plugin.WorkspaceSynchronizer.SynchronizationDetailFactory;
import milkman.utils.fxml.FxmlUtil;

public class CreateWorkspaceDialog {
	@FXML VBox syncDetailsArea;
	@FXML JFXComboBox<SynchronizationDetailFactory> syncSelector;
	@FXML JFXTextField workspaceNameInput;
	private Dialog dialog;

	SynchronizationDetailFactory selectedSynchronizer = null;
	
	private Toaster toaster;
	private Workspace workspace;
	private SyncDetails syncDetails;
	
	public void showAndWait(List<WorkspaceSynchronizer> synchronizers, Toaster toaster) {
		this.toaster = toaster;
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/CreateWorkspaceDialog.fxml", this);
		synchronizers.forEach(i -> syncSelector.getItems().add(i.getDetailFactory()));
		
		//default to first item
		if (syncSelector.getItems().size() > 0) {
			SynchronizationDetailFactory defaultSync = syncSelector.getItems().get(0);
			syncSelector.setValue(defaultSync);
			syncDetailsArea.getChildren().add(defaultSync.getSyncDetailsControls());
			selectedSynchronizer = defaultSync;
		}
		
		syncSelector.setConverter(new StringConverter<SynchronizationDetailFactory>() {
			
			@Override
			public String toString(SynchronizationDetailFactory object) {
				return object.getName();
			}
			
			@Override
			public SynchronizationDetailFactory fromString(String string) {
				return null;
			}
		});
		syncSelector.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> {
			if (o != v && v != null) {
				selectedSynchronizer = v;
				syncDetailsArea.getChildren().clear();
				syncDetailsArea.getChildren().add(v.getSyncDetailsControls());
			} 
		});
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}
	
	
	@FXML public void onClose() {
		dialog.close();
	}
	
	@FXML public void onCreate() {
		if (selectedSynchronizer != null && workspaceNameInput.validate()) {
			try{
				syncDetails = selectedSynchronizer.createSyncDetails();
				dialog.close();
			} catch (Throwable t) {
				toaster.showToast(ExceptionUtils.getRootCauseMessage(t));
			}
		}
	}
	
	
	public SyncDetails getSyncDetails() {
		return syncDetails;
	}
	
	public String getWorkspaceName() {
		return workspaceNameInput.getText();
	}
	
	public boolean wasCancelled() {
		return syncDetails == null;
	}
	
	
	
}
