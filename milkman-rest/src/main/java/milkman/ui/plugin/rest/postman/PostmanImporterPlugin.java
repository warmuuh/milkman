package milkman.ui.plugin.rest.postman;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.poynt.postman.model.PostmanCollection;
import co.poynt.postman.model.PostmanContainer;
import co.poynt.postman.model.PostmanFolder;
import co.poynt.postman.model.PostmanItem;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

@Slf4j
public class PostmanImporterPlugin implements ImporterPlugin {

	private TextArea textArea;

	private PostmanImporter importer = new PostmanImporter();
	
	@Override
	public String getName() {
		return "Postman (v2.1) > Raw Text";
	}

	@Override
	public Node getImportControls() {
		textArea = new TextArea();
		return textArea;
	}

	@Override
	public boolean importInto(Workspace workspace, Toaster toaster) {
		
		try {
			Collection collection = importer.importString(textArea.getText());
			workspace.getCollections().add(collection);
			toaster.showToast("Imported Collection: " + collection.getName());
		} catch (Exception e) {
			log.error("Failed to import postman collection", e);
			toaster.showToast("Failed import: " + ExceptionUtils.getRootCauseMessage(e));
		}
		
		return true;
	}
}
