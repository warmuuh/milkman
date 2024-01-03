package milkman.ui.plugins.management.options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static milkman.ExceptionDialog.showExceptionDialog;
import static milkman.ui.plugins.management.options.PluginManagementMessages.ERROR_MESSAGE_PLUGIN_INSTALLATION_FAILED;
import static milkman.ui.plugins.management.options.PluginManagementMessages.SUCCESS_MESSAGE_PLUGIN_INSTALLATION;

@Slf4j
public class PluginManager {

	public Optional<PluginMetaData> installPlugin(File pluginFile) {
		if (isValidPluginFile(pluginFile)) {
			return copyPluginFile(pluginFile);
		} else {
			new Alert(Alert.AlertType.WARNING, "The selected file is not a valid Milkman Plugin file", ButtonType.CLOSE).showAndWait();
		}
		return Optional.empty();
	}

	private boolean isValidPluginFile(File result) {
		return result.exists()
			&& result.isFile()
			&& result.canRead()
			&& hasValidManifest(result);
	}

	private boolean hasValidManifest(File result) {
		return extractPluginMetaData(result.toPath()).map(PluginMetaData::id).map(StringUtils::isNotBlank).orElse(false);
	}

	private Optional<PluginMetaData> copyPluginFile(File source) {
		try {
			var milkmanInstallationDirectory = MilkmanInstallationLocationExtractor.getMilkmanInstallationDirectory();
			var copiedFile = copyPluginFileToTargetDirectory(source, Path.of(milkmanInstallationDirectory, "plugins", source.getName()));
			var pluginMetaData = extractPluginMetaData(copiedFile);
			pluginMetaData
				.map(this::createSuccessInformation)
				.orElseGet(() -> createErrorInformation(milkmanInstallationDirectory))
				.showAndWait();
			return pluginMetaData;
		} catch (IOException e) {
			log.warn("Error copying the plugin file", e);
			showExceptionDialog("Warning", "Error copying the plugin", "There was an error copying the selected plugin file", e);
		}
		return Optional.empty();
	}

	private Path copyPluginFileToTargetDirectory(File source, Path target) throws IOException {
		return Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
	}

	public List<PluginMetaData> getInstalledPlugins() {
		try (var files = Files.list(Path.of(MilkmanInstallationLocationExtractor.getMilkmanInstallationDirectory(), "plugins"))) {
			return filterPlugins(files);
		} catch (Exception e) {
			log.warn("Error fetch plugin files", e);
			showExceptionDialog("Warning", "Reading installed plugins failed", "There was an error reading the installed plugins", e);
		}
		return new LinkedList<>();
	}

	private List<PluginMetaData> filterPlugins(Stream<Path> files) {
		return files
			.filter(p -> StringUtils.endsWithIgnoreCase(p.getFileName().toString(), ".jar"))
			.map(this::extractPluginMetaData)
			.flatMap(Optional::stream)
			.sorted(Comparator.comparing(PluginMetaData::id))
			.collect(Collectors.toCollection(LinkedList::new));
	}

	private Optional<PluginMetaData> extractPluginMetaData(Path path) {
		try (var file = new JarFile(path.toFile())) {
			return Optional
				.ofNullable(file.getManifest())
				.map(manifest -> manifest.getAttributes("milkman.plugin"))
				.map(attributes -> new PluginMetaData(attributes.getValue("Id"), ListParser.parseList(attributes.getValue("Author")), path))
				.filter(pluginMetaData -> StringUtils.isNotBlank(pluginMetaData.id()));
		} catch (IOException e) {
			log.warn("Error reading manifest of JAR file {}", path, e);
		}
		return Optional.empty();
	}

	public boolean uninstallPlugin(PluginMetaData plugin) {
		try {
			Files.delete(plugin.filename());
			return true;
		} catch (Exception e) {
			log.warn("Unable to delete the plugin {} ({})", plugin.id(), plugin.filename());
			showExceptionDialog(
				"Error uninstalling the plugin",
				"While uninstalling the plugin %s, an error has been encountered. Make sure your user has write permissions in the plugins directory of milkman (%s/plugins)".formatted(
					plugin.id(),
					MilkmanInstallationLocationExtractor.getMilkmanInstallationDirectory()),
				e);
		}
		return false;
	}

	private Alert createSuccessInformation(PluginMetaData pluginMetaData) {
		return new Alert(
			Alert.AlertType.INFORMATION,
			SUCCESS_MESSAGE_PLUGIN_INSTALLATION.apply(pluginMetaData),
			ButtonType.CLOSE);
	}

	private Alert createErrorInformation(String milkmanInstallationDirectory) {
		return new Alert(
			Alert.AlertType.WARNING,
			ERROR_MESSAGE_PLUGIN_INSTALLATION_FAILED.apply(milkmanInstallationDirectory),
			ButtonType.CLOSE);
	}

}
