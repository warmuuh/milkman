package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;

import java.util.List;

import static milkman.utils.fxml.FxmlBuilder.*;

public class ImportDialog {
	 VBox importerArea;
	 JFXComboBox<ImporterPlugin> importerSelector;
	private Dialog dialog;

	private ImporterPlugin selectedImporter = null;
	private Toaster toaster;
	private Workspace workspace;
	
	public void showAndWait(List<ImporterPlugin> importers, Toaster toaster, Workspace workspace) {
		this.toaster = toaster;
		this.workspace = workspace;
		JFXDialogLayout content = new ImportDialogFxml(this);
		importerSelector.setPromptText("Select Importer");
		importers.forEach(i -> importerSelector.getItems().add(i));
		importerSelector.setConverter(new StringConverter<ImporterPlugin>() {
			
			@Override
			public String toString(ImporterPlugin object) {
				return object.getName();
			}
			
			@Override
			public ImporterPlugin fromString(String string) {
				return null;
			}
		});
		importerSelector.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> {
			if (o != v && v != null) {
				selectedImporter = v;
				importerArea.getChildren().clear();
				importerArea.getChildren().add(v.getImportControls());
			} 
		});
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}
	
	
	 public void onClose() {
		dialog.close();
	}
	
	 public void onImport() {
		if (selectedImporter != null) {
			if (selectedImporter.importInto(workspace, toaster)) {
				dialog.close();
			}
		}
	}



	public class ImportDialogFxml extends JFXDialogLayout {
		public ImportDialogFxml(ImportDialog controller){
			setHeading(label("Import"));

			var vbox = new FxmlBuilder.VboxExt();
			controller.importerSelector = vbox.add(new JFXComboBox());
			controller.importerArea = vbox.add(vbox("importerArea"));
			setBody(vbox);

			setActions(submit(controller::onImport, "Import"),
					cancel(controller::onClose, "Close"));

		}
	}
}
