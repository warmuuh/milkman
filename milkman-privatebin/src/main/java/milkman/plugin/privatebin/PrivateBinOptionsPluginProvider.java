package milkman.plugin.privatebin;

import lombok.Data;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class PrivateBinOptionsPluginProvider implements OptionPageProvider<PrivateBinOptionsPluginProvider.PrivateBinOptions> {

	@Data
	public static class PrivateBinOptions implements OptionsObject {
		private String privateBinUrl = "https://privatebin.net/?";
	}

	
	private static PrivateBinOptions currentOptions = new PrivateBinOptions();
	public static PrivateBinOptions options() {
		return currentOptions;
	}
	
	@Override
	public PrivateBinOptions getOptions() {
		return currentOptions;
	}

	@Override
	public void setOptions(PrivateBinOptions options) {
		currentOptions = options;		
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		return builder.page("PrivateBin", getOptions())
				.section("Private Bin Installation")
					.textInput("PrivateBin Url", PrivateBinOptions::getPrivateBinUrl, PrivateBinOptions::setPrivateBinUrl)
				.endSection()
				.build();
	}

	@Override
	public int getOrder() {
		return 300;
	}
	
	
}
