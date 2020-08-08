package milkman.ui.main.options;

import lombok.Data;
import milkman.ui.main.ThemeSwitcher;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class CoreApplicationOptionsProvider implements OptionPageProvider<CoreApplicationOptionsProvider.CoreApplicationOptions>{

	@Data
	public static class CoreApplicationOptions implements OptionsObject {
		private boolean autoformatContent = true;
		private boolean checkForUpdates = true;
		private String theme = "Milkman";
		private boolean disableAnimations = false;
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
					.toggle("Disable Animations", CoreApplicationOptions::isDisableAnimations, CoreApplicationOptions::setDisableAnimations)
				.endSection()
				.section("Code Editor/Viewer")
					.toggle("Autoformat Content", CoreApplicationOptions::isAutoformatContent, this::toggleAnimations)
				.endSection()
				.build();
	}
	
	private void setTheme(CoreApplicationOptions options, String themeName) {
		themeSwitcher.setTheme(themeName, options.isDisableAnimations());
		options.setTheme(themeName);
	}

	private void toggleAnimations(CoreApplicationOptions options, boolean disableAnimations){
		themeSwitcher.setTheme(options.getTheme(), disableAnimations);
		options.setDisableAnimations(disableAnimations);
	}

	@Override
	public int getOrder() {
		return 100;
	}
	
	
	
}
