package milkman.ui.plugin.rest.postman;


import java.io.InputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;

@Slf4j
public class PostmanEnvironmentImporterPlugin implements ImporterPlugin {

	private ImportControl importCtrl;

	private PostmanImporter importer = new PostmanImporter();
	
	@Override
	public String getName() {
		return "Postman (v2.1) Environment";
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
			Environment env = importer.importEnvironment(stream);
			workspace.getEnvironments().add(env);
			toaster.showToast("Imported Environment: " + env.getName());
		} catch (Exception e) {
			log.error("Failed to import postman environment", e);
			toaster.showToast("Failed import: " + ExceptionUtils.getRootCauseMessage(e));
		}
		
		return true;
	}
}
