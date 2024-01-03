package milkman.ui.plugins.management.options;

import java.nio.file.Path;
import java.util.List;

public record PluginMetaData(String id, List<String> authors, Path filename) {
}
