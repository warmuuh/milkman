package milkman.ui.plugins.management.options;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.ui.plugin.OptionsObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginsManagementOptions implements OptionsObject {
	private List<PluginMetaData> plugins;
}
