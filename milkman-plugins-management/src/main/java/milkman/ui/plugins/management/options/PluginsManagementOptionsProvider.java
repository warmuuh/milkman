package milkman.ui.plugins.management.options;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugins.management.market.GithubApiClient;
import milkman.ui.plugins.management.market.MarketplaceDialog;
import milkman.utils.fxml.FxmlUtil;

import static milkman.ui.plugins.management.options.PluginManagementMessages.CONFIRMATION_MESSAGE_UNINSTALL_PLUGIN;

@Slf4j
public class PluginsManagementOptionsProvider implements OptionPageProvider<PluginsManagementOptions> {

	private final List<PluginMetaData> installedPlugins;
	private final PluginManager pluginManager = new PluginManager();

	public PluginsManagementOptionsProvider() {
		this.installedPlugins = new LinkedList<>();
	}

	@Override
	public PluginsManagementOptions getOptions() {
		reloadInstalledPlugins();
		return new PluginsManagementOptions(installedPlugins);
	}

	private void reloadInstalledPlugins() {
		installedPlugins.clear();
		installedPlugins.addAll(pluginManager.getInstalledPlugins());
	}

	@Override
	public void setOptions(PluginsManagementOptions options) {
		//intentionally left empty, as the "options" of this plugin are not stored in the database but read in on showing the setting dialog
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		var columnValueProviders = new TreeMap<String, Function<PluginMetaData, String>>(Comparator.reverseOrder());
		columnValueProviders.putAll(Map.of("Name", PluginMetaData::id, "Authors", data -> String.join(", ", data.authors())));
		return builder
			.page("Plugins", getOptions())
			.section("Plugins")
			.<PluginMetaData>list()
				.itemProvider(PluginsManagementOptions::getPlugins)
				.columnValueProviders(columnValueProviders)
				.columnValueProviders(columnValueProviders)
				.newValueProvider(this::installPlugin)
				.vetoableListChangeListener(this::uninstallPlugin)
			.endList()
			.button("Marketplace...", this::installFromMarketplace)
			.endSection()
			.build();
	}

	private void installFromMarketplace() {
		var marketplaceDialog = new MarketplaceDialog();
		marketplaceDialog.showAndWait();
		if (!marketplaceDialog.isCancelled()) {
			GithubApiClient.MarketplacePlugin chosenPlugin = marketplaceDialog.getChosenPlugin();
			pluginManager.downloadAndInstallPlugin(chosenPlugin.artifactFilename(), chosenPlugin.artifactUrl());
		}
	}

	private PluginMetaData installPlugin() {
		var fileChooser = new FileChooser();
		fileChooser.setTitle("Use Plugin");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plugin-Files (*.jar)", "*.jar"));
		var result = fileChooser.showOpenDialog(FxmlUtil.getPrimaryStage());
		if (result == null) {
			return null;
		}
		return pluginManager.installPlugin(result).orElse(null);
	}

	private boolean uninstallPlugin(PluginMetaData element) {
		var alert = new Alert(AlertType.CONFIRMATION, CONFIRMATION_MESSAGE_UNINSTALL_PLUGIN.apply(element), ButtonType.YES, ButtonType.NO);
		var result = alert.showAndWait();
		return result
			.filter(ButtonType.YES::equals)
			.map(e -> pluginManager.uninstallPlugin(element))
			.orElse(false);
	}
}
