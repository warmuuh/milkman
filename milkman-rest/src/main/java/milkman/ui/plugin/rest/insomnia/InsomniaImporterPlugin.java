package milkman.ui.plugin.rest.insomnia;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.Node;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.PluginManagerAware;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.rest.postman.ImportControl;
import milkman.ui.plugin.rest.postman.importers.PostmanDumpImporter;
import milkman.ui.plugin.rest.postman.importers.PostmanImporterV10;
import milkman.ui.plugin.rest.postman.importers.PostmanImporterV21;
import org.apache.commons.io.IOUtils;
import org.reactfx.util.Try;

@Slf4j
public class InsomniaImporterPlugin implements ImporterPlugin, PluginManagerAware {

	private ImportControl importCtrl;
	private UiPluginManager manager;

	@Override
	public String getName() {
		return "Insomnia Import V4 (json)";
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
		importCollectionV4(content, workspace, toaster)
		.ifFailure(e -> {
			e.printStackTrace();
			toaster.showToast("Failed to import data. unrecognized format.");
		});

		return true;
	}

	public Try<Void> importCollectionV4(String content, Workspace workspace, Toaster toaster) {
		return Try.tryGet(() -> {
			List<InsomniaRequestImporter> importers = manager.loadSpiInstances(InsomniaRequestImporter.class);
			InsomniaImporterV4 importer = new InsomniaImporterV4(importers);
			List<Collection> collections = new LinkedList<>();
			List<Environment> environments = new LinkedList<>();
			importer.importCollection(content, collections, environments);

			workspace.getCollections().addAll(collections);
			toaster.showToast("Imported Collections: " + collections.stream().map(Collection::getName).toList());
			workspace.getEnvironments().addAll(environments);
			toaster.showToast("Imported Environments: " + environments.stream().map(Environment::getName).toList());

			return null;
		});
	}

	@Override
	public void setPluginManager(UiPluginManager manager) {
		this.manager = manager;
	}
}
