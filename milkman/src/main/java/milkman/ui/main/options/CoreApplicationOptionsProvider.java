package milkman.ui.main.options;

import java.util.Arrays;

import lombok.Data;
import milkman.ui.main.ThemeSwitcher;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class CoreApplicationOptionsProvider implements OptionPageProvider<CoreApplicationOptionsProvider.CoreApplicationOptions>{

	@Data
	public static class CoreApplicationOptions implements OptionsObject {
		private boolean autoformatContent = false;
		private boolean checkForUpdates = true;
		private String theme = "Milkman";
	} 

	private static CoreApplicationOptions currentOptions = new CoreApplicationOptions();
	public static CoreApplicationOptions options() {
		return currentOptions;
	}
	
	
	private static ThemeSwitcher themeSwitcher;
	public static void setThemeSwitcher(ThemeSwitcher switcher) {
		themeSwitcher = switcher;
	}
	
	
	@Override
	public CoreApplicationOptions getOptions() {
		return currentOptions;
	}

	@Override
	public void setOptions(CoreApplicationOptions options) {
		currentOptions = options;
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		return builder.page("Application", getOptions())
				.section("General")
					.toggle("Check for Updates", CoreApplicationOptions::isCheckForUpdates, CoreApplicationOptions::setCheckForUpdates)
					.selection("Theme", CoreApplicationOptions::getTheme, this::setTheme, themeSwitcher.getThemes())
				.endSection()
				.section("Code Editor/Viewer")
					.toggle("Autoformat Content", CoreApplicationOptions::isAutoformatContent, CoreApplicationOptions::setAutoformatContent)
				.endSection()
				.build();
	}
	
	private void setTheme(CoreApplicationOptions options, String themeName) {
		themeSwitcher.setTheme(themeName);
		options.setTheme(themeName);
	}


	@Override
	public int getOrder() {
		return 100;
	}
	
	
	
}
