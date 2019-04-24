package milkman.ui.main.dialogs;

import java.util.List;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.utils.fxml.FxmlUtil;

public class ExportDialog {
	@FXML VBox exportArea;
	@FXML JFXComboBox<RequestExporterPlugin> exportSelector;
	@FXML JFXButton exportBtn;
	
	
	private Dialog dialog;

	private RequestExporterPlugin selectedExporter = null;
	private Toaster toaster;
	private RequestContainer request;
	
	public void showAndWait(List<RequestExporterPlugin> exporters, Toaster toaster, RequestContainer request) {
		this.toaster = toaster;
		this.request = request;
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/ExportDialog.fxml", this);
		exportSelector.setPromptText("Select Exporter");
		exporters.forEach(exportSelector.getItems()::add);
		exportSelector.setConverter(new StringConverter<RequestExporterPlugin>() {
			
			@Override
			public String toString(RequestExporterPlugin object) {
				return object.getName();
			}
			
			@Override
			public RequestExporterPlugin fromString(String string) {
				return null;
			}
		});
		exportSelector.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> {
			if (o != v && v != null) {
				selectedExporter = v;
				exportArea.getChildren().clear();
				exportArea.getChildren().add(v.getRoot(request));
				exportBtn.setDisable(v.isAdhocExporter());
			} 
		});
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}
	
	
	@FXML public void onClose() {
		dialog.close();
	}
	
	@FXML public void onExport() {
		if (selectedExporter != null) {
			if (selectedExporter.doExport(request, toaster)) {
				dialog.close();
			}
		}
	}
}
