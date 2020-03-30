package milkman.plugin.scripting;

import lombok.Data;
import milkman.ui.main.ThemeSwitcher;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

import java.util.LinkedList;
import java.util.List;

public class ScriptOptionsProvider implements OptionPageProvider<ScriptOptionsProvider.ScriptOptions>{

	@Data
	public static class ScriptOptions implements OptionsObject {
		private List<String> preloadScripts = new LinkedList<>();
	}

	private static ScriptOptions currentOptions = new ScriptOptions();
	public static ScriptOptions options() {
		return currentOptions;
	}
	
	@Override
	public ScriptOptions getOptions() {
		return currentOptions;
	}

	@Override
	public void setOptions(ScriptOptions options) {
		currentOptions = options;
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		return builder.page("Scripting", getOptions())
				.section("Preload Scripts")
					.list(ScriptOptions::getPreloadScripts)
				.endSection()
				.build();
	}


	@Override
	public int getOrder() {
		return 700;
	}
	
	
	
}
