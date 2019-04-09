package milkman.ui.plugin.rest.postman;


import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;

@Slf4j
public class PostmanDumpImporterPlugin implements ImporterPlugin {

	private TextArea textArea;

	private PostmanDumpImporter importer = new PostmanDumpImporter();
	
	@Override
	public String getName() {
		return "Postman (v2.1) Dump";
	}

	@Override
	public Node getImportControls() {
		textArea = new TextArea();
		return textArea;
	}

	@Override
	public boolean importInto(Workspace workspace, Toaster toaster) {
		
		try {
			Workspace importedStuff = importer.importDump(textArea.getText());
			workspace.getCollections().addAll(importedStuff.getCollections());
			workspace.getEnvironments().addAll(importedStuff.getEnvironments());
			toaster.showToast("Import successful");
		} catch (Exception e) {
			log.error("Failed to import postman collection", e);
			toaster.showToast("Failed import: " + ExceptionUtils.getRootCauseMessage(e));
		}
		
		return true;
	}
}
