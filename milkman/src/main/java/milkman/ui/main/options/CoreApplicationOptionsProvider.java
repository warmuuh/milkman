package milkman.ui.main.options;

import lombok.Data;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class CoreApplicationOptionsProvider implements OptionPageProvider<CoreApplicationOptionsProvider.CoreApplicationOptions>{

	@Data
	public static class CoreApplicationOptions implements OptionsObject {
		private boolean autoformatContent;
	}

	private static CoreApplicationOptions currentOptions = new CoreApplicationOptions();
	public static CoreApplicationOptions options() {
		return currentOptions;
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
					.toggle("Autoformat Content", CoreApplicationOptions::isAutoformatContent, CoreApplicationOptions::setAutoformatContent)
				.endSection()
				.build();
	}

	
	
}
