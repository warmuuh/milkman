package milkman.ui.plugins.management.options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import milkman.ExceptionDialog;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.GenericBinding;
import org.apache.commons.lang3.StringUtils;

import static milkman.utils.fxml.FxmlBuilder.button;
import static milkman.utils.fxml.FxmlBuilder.icon;
import static org.apache.commons.io.FilenameUtils.removeExtension;

@Slf4j
public class PluginsManagementOptionsProvider implements OptionPageProvider<PluginsManagementOptions> {

	private PluginsManagementOptions options = new PluginsManagementOptions();

	@Override
	public PluginsManagementOptions getOptions() {
		return options;
	}

	@Override
	public void setOptions(PluginsManagementOptions options) {
		this.options = options;
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		var pane = new OptionDialogPane("Plugins", List.of(GenericBinding.of(this::getInstalledPlugins, null)));
		pane.getStyleClass().add("options-page");
		var list = new JFXListView<String>();
		list.getItems().addAll(getInstalledPlugins(null));
		pane.getChildren().add(list);
		var addSection = new StackPane();
		var button = button(icon(FontAwesomeIcon.PLUS, "1.5em"), this::installPlugin);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.getStyleClass().add("btn-add-entry");
		StackPane.setAlignment(button, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(button, new Insets(20, 20, 5, 0));

		addSection.getChildren().add(button);
		pane.getChildren().add(addSection);
		return pane;
	}

	private void installPlugin() {
		var fileChooser = new FileChooser();
		fileChooser.setTitle("Use Plugin");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plugin-Files (*.jar)", "*.jar"));
		var result = fileChooser.showOpenDialog(FxmlUtil.getPrimaryStage());
		if (result == null) {
			return;
		}
		if (isValidPluginFile(result)) {
			copyPluginFile(result);
		} else {
			new Alert(Alert.AlertType.WARNING, "The selected file is not a valid Milkman Plugin file", ButtonType.CLOSE).showAndWait();
		}
	}

	private boolean isValidPluginFile(File result) {
		return result.exists()
			&& result.isFile()
			&& result.canRead()
			&& hasValidManifest(result);
	}

	private boolean hasValidManifest(File result) {
		return StringUtils.isNotBlank(extractPluginNameFromManifest(result.toPath()));
	}

	private void copyPluginFile(File source) {
		try {
			var pluginName = extractPluginName(Files.copy(source.toPath(), Path.of(MilkmanInstallationLocationExtractor.getMilkmanInstallationDirectory(), "plugins", source.getName()), StandardCopyOption.REPLACE_EXISTING));
			new Alert(Alert.AlertType.INFORMATION, "The plugin %s was successfully installed. Please restart Milkman to enable the new plugin.".formatted(pluginName), ButtonType.CLOSE).showAndWait();
		} catch (IOException e) {
			log.warn("Error copying the plugin file", e);
			new ExceptionDialog("Warning", "Error copying the plugin", "There was an error copying the selected plugin file", e).showAndWait();
		}
	}

	private List<String> getInstalledPlugins(PluginsManagementOptions pluginsManagementOptions) {
		try {
			return Files
				.list(Path.of(MilkmanInstallationLocationExtractor.getMilkmanInstallationDirectory(), "plugins"))
				.filter(p -> StringUtils.endsWithIgnoreCase(p.getFileName().toString(), ".jar"))
				.map(this::extractPluginName)
				.sorted()
				.toList();
		} catch (Exception e) {
			log.warn("Error fetch plugin files", e);
			new ExceptionDialog("Warning", "Reading installed plugins failed", "There was an error reading the installed plugins", e).showAndWait();
		}
		return List.of();
	}

	private String extractPluginName(Path path) {
		var plainFileName = removeExtension(path.getFileName().toString());
		return StringUtils.defaultIfBlank(extractPluginNameFromManifest(path), plainFileName);
	}

	private static String extractPluginNameFromManifest(Path path) {
		try (var file = new JarFile(path.toFile())) {
			return Optional
				.ofNullable(file.getManifest())
				.map(manifest -> manifest.getAttributes("milkman.plugin"))
				.map(attributes -> attributes.getValue("Id"))
				.filter(StringUtils::isNotBlank)
				.orElse(null);
		} catch (IOException e) {
			log.warn("Error reading manifest of JAR file {}", path, e);
		}
		return null;
	}
}
