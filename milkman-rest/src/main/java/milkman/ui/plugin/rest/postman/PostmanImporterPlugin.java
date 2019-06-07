package milkman.ui.plugin.rest.postman;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import io.vavr.control.Try;
import javafx.scene.Node;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.rest.postman.importers.PostmanDumpImporter;
import milkman.ui.plugin.rest.postman.importers.PostmanImporterV10;
import milkman.ui.plugin.rest.postman.importers.PostmanImporterV21;

@Slf4j
public class PostmanImporterPlugin implements ImporterPlugin {

	private ImportControl importCtrl;

	@Override
	public String getName() {
		return "Postman Import";
	}

	@Override
	public Node getImportControls() {
		importCtrl = new ImportControl();
		return importCtrl;
	}

	@Override
	@SneakyThrows
	public boolean importInto(Workspace workspace, Toaster toaster) {
		InputStream stream = importCtrl.getInput();
		if (stream == null) {
			toaster.showToast("Missing or invalid data");
			return false;
		}
		String content = IOUtils.toString(stream);
		importEnvironment(content, workspace, toaster)
			.onFailure(e -> log.warn("environment import failed", e))
		.recoverWith(e -> importCollectionV10(content, workspace, toaster))
			.onFailure(e -> log.warn("collection v1 import failed", e))
		.recoverWith(e -> importCollevtionV21(content, workspace, toaster))
			.onFailure(e -> log.warn("collection v2.1 import failed", e))
		.recoverWith(e -> importPostmanDump(content, workspace, toaster))
			.onFailure(e -> log.warn("dump import failed", e))
		.orElseRun(e -> toaster.showToast("Failed to import data. unrecognized format."));

		return true;
	}

	public Try<Void> importEnvironment(String content, Workspace workspace, Toaster toaster) {
		return Try.run(() -> {
			PostmanImporterV21 importer = new PostmanImporterV21();
			Environment env = importer.importEnvironment(content);
			workspace.getEnvironments().add(env);
			toaster.showToast("Imported Environment: " + env.getName());
		});
	}

	public Try<Void> importCollectionV10(String content, Workspace workspace, Toaster toaster) {
		return Try.run(() -> {
			PostmanImporterV10 importer = new PostmanImporterV10();
			Collection collection = importer.importCollection(content);
			workspace.getCollections().add(collection);
			toaster.showToast("Imported Collection (V1.0): " + collection.getName());
		});
	}

	public Try<Void> importCollevtionV21(String content, Workspace workspace, Toaster toaster) {
		return Try.run(() -> {
			PostmanImporterV21 importer = new PostmanImporterV21();
			Collection collection = importer.importCollection(content);
			workspace.getCollections().add(collection);
			toaster.showToast("Imported Collection (V2.1): " + collection.getName());
		});
	}

	public Try<Void> importPostmanDump(String content, Workspace workspace, Toaster toaster) {
		return Try.run(() -> {
			PostmanDumpImporter importer = new PostmanDumpImporter();
			Workspace importedStuff = importer.importDump(content);
			workspace.getCollections().addAll(importedStuff.getCollections());
			workspace.getEnvironments().addAll(importedStuff.getEnvironments());
			toaster.showToast("Imported Postman Dump");
		});
	}

}
