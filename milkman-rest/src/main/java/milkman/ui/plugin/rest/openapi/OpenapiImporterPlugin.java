package milkman.ui.plugin.rest.openapi;

import java.io.InputStream;
import javafx.scene.Node;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.rest.postman.ImportControl;
import org.apache.commons.io.IOUtils;
import org.reactfx.util.Try;

@Slf4j
public class OpenapiImporterPlugin implements ImporterPlugin {

    private ImportControl importCtrl;

    @Override
    public String getName() {
        return "OpenApi Import (YAML)";
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
        importSpecV30(content, workspace, toaster)
////			.onFailure(e -> log.warn("environment import failed", e))
//		.orElse(() -> importCollectionV10(content, workspace, toaster))
////			.onFailure(e -> log.warn("collection v1 import failed", e))
//		.orElse(() -> importCollevtionV21(content, workspace, toaster))
////			.onFailure(e -> log.warn("collection v2.1 import failed", e))
//		.orElse(() -> importPostmanDump(content, workspace, toaster))
////			.onFailure(e -> log.warn("dump import failed", e))
                .ifFailure(e -> toaster.showToast("Failed to import data. unrecognized format."));

        return true;
    }


    public Try<Void> importSpecV30(String content, Workspace workspace, Toaster toaster) {
        return Try.tryGet(() -> {
            OpenapiImporterV30 importer = new OpenapiImporterV30();
            var result = importer.importCollection(content, workspace.getEnvironments());
            Collection collection = result.getLeft();
            var newEnvKeys = result.getRight();

            //TODO: for now, we just import everything into the active environment. Later, we let the user choose which key goes where.
            if (!newEnvKeys.isEmpty()) {
                workspace.getEnvironments().stream()
                        .filter(e -> e.isActive())
                        .findAny()
                        .ifPresentOrElse(
                                e -> newEnvKeys.forEach(k -> e.setOrAdd(k.getKeyName(), k.getKeyValue())),
                                () -> toaster.showToast("No Active environment found for importing server variables."));
            }

            workspace.getCollections().add(collection);

            toaster.showToast("Imported Openapi Spec (V3.0): " + collection.getName());
            return null;
        });
    }


}
