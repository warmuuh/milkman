package milkman.ui.main.options;

import lombok.Data;
import milkman.ui.main.ThemeSwitcher;
import milkman.ui.main.options.CoreApplicationOptionsProvider.CoreApplicationOptions;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CoreApplicationOptionsProvider implements OptionPageProvider<CoreApplicationOptions>{

	@Data
	public static class CoreApplicationOptions implements OptionsObject {
		private boolean autoformatContent = true;
		private boolean checkForUpdates = true;
		private String theme = "Milkman";
		private boolean disableAnimations;
		private boolean horizontalLayout;
		private boolean debug;
		private UiPrefs uiPrefs = new UiPrefs();
		private boolean disableColorfulUi;

		@Data
		public static class UiPrefs {
			private Map<String, Integer> tableColumnsWidth = new HashMap<>();

			public void setWidthForColumn(String tableId, int columnIdx, int width){
				var columnId = tableId + "-" + columnIdx;
				tableColumnsWidth.put(columnId, width);
			}

			public Optional<Integer> getPrefWidth(String tableId, int columnIdx){
				var columnId = tableId + "-" + columnIdx;
				return Optional.ofNullable(tableColumnsWidth.get(columnId));
			}
		}
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
				.toggle("Disable Colorful Ui", CoreApplicationOptions::isDisableColorfulUi, CoreApplicationOptions::setDisableColorfulUi)
				.toggle("Enable Debug Output", CoreApplicationOptions::isDebug, CoreApplicationOptions::setDebug)
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
