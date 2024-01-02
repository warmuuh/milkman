package milkman.ui.plugins.management.options;

import java.util.function.Function;

import lombok.experimental.UtilityClass;

@UtilityClass
class PluginManagementMessages {
	static final Function<String, String> ERROR_MESSAGE_PLUGIN_INSTALLATION_FAILED =
		"""
			The plugin was not successfully installed.
						
			Please check that your user has write permission in the Milkman installation directory
			%s
			and try again.
			"""::formatted;

	static final Function<PluginMetaData, String> SUCCESS_MESSAGE_PLUGIN_INSTALLATION = pluginMetaData ->
		"""
			The plugin
						
			%s
			by %s
						
			was successfully installed.
						
			Please restart Milkman to enable the new plugin.
			""".formatted(pluginMetaData.id(), String.join(", ", pluginMetaData.authors()));

	static final Function<PluginMetaData, String> CONFIRMATION_MESSAGE_UNINSTALL_PLUGIN = pluginMetaData ->
		"""
			Do you really want to delete the plugin
						
			%s
			by %s?
			""".formatted(pluginMetaData.id(), String.join(", ", pluginMetaData.authors()));

}
