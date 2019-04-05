package milkman.ui.plugin.rest.postman;


import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;

@Slf4j
public class PostmanEnvironmentImporterPlugin implements ImporterPlugin {

	private TextArea textArea;

	private PostmanImporter importer = new PostmanImporter();
	
	@Override
	public String getName() {
		return "Postman (v2.1) Environment";
	}

	@Override
	public Node getImportControls() {
		textArea = new TextArea();
		return textArea;
	}

	@Override
	public boolean importInto(Workspace workspace, Toaster toaster) {
		
		try {
			Environment env = importer.importEnvironment(textArea.getText());
			workspace.getEnvironments().add(env);
			toaster.showToast("Imported Environment: " + env.getName());
		} catch (Exception e) {
			log.error("Failed to import postman environment", e);
			toaster.showToast("Failed import: " + ExceptionUtils.getRootCauseMessage(e));
		}
		
		return true;
	}
}
