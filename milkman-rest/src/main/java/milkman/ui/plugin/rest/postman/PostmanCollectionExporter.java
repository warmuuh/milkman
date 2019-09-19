package milkman.ui.plugin.rest.postman;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import milkman.domain.Collection;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.CollectionExporterPlugin;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.postman.exporters.PostmanExporterV21;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.javafx.RetentionFileChooser;

public class PostmanCollectionExporter implements CollectionExporterPlugin {

	private TextArea textArea;
	
	@Override
	public String getName() {
		return "Postman Collection";
	}

	@Override
	public Node getRoot(Collection request, Templater templater) {
		PostmanExporterV21 exporter = new PostmanExporterV21();
		String exportedData = exporter.export(request);
		RetentionFileChooser.getInstance().setInitialFileName(request.getName() + ".postman_collection.json");

		textArea = new TextArea(exportedData);
		Button saveBtn = new Button("save as...");
		saveBtn.setOnAction(e -> {
			File f = RetentionFileChooser.showSaveDialog(FxmlUtil.getPrimaryStage());
			if (f != null && !f.isDirectory()) {
				saveExportAsFile(textArea.getText(), f);
			}
		});
		return new VBox(textArea, saveBtn);
	}

	@SneakyThrows
	private void saveExportAsFile(String text, File f) {
		FileOutputStream os = new FileOutputStream(f);
		IOUtils.write(text, os);
		os.close();
	}

	@Override
	public boolean isAdhocExporter() {
		return true;
	}

	@Override
	public boolean doExport(Collection collection, Templater templater, Toaster toaster) {
		throw new UnsupportedOperationException("not implemented bc adhoc exporter");
	}

	
}
