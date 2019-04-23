package milkman.ui.plugin.rest.postman;


import java.io.InputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;

@Slf4j
public class PostmanDumpImporterPlugin implements ImporterPlugin {

	private ImportControl importCtrl;

	private PostmanDumpImporter importer = new PostmanDumpImporter();
	
	@Override
	public String getName() {
		return "Postman (v2.1) Dump";
	}

	@Override
	public Node getImportControls() {
		importCtrl = new ImportControl();
		return importCtrl;
	}

	@Override
	public boolean importInto(Workspace workspace, Toaster toaster) {

		InputStream stream = importCtrl.getInput();
		if (stream == null) {
			toaster.showToast("Missing or invalid data");
			return false;
		}
		
		try {
			Workspace importedStuff = importer.importDump(stream);
			workspace.getCollections().addAll(importedStuff.getCollections());
			workspace.getEnvironments().addAll(importedStuff.getEnvironments());
			toaster.showToast("Import successful");
			return true;
		} catch (Exception e) {
			log.error("Failed to import postman collection", e);
			toaster.showToast("Failed import: " + ExceptionUtils.getRootCauseMessage(e));
		}
		return false;
	}
}
