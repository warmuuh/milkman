package milkman.ui.plugin.rest;

import lombok.Data;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class HttpOptionsPluginProvider implements OptionPageProvider<HttpOptionsPluginProvider.HttpOptions> {

	@Data
	public static class HttpOptions implements OptionsObject {
		private String proxyUrl = "";
		private boolean askForProxyAuth = false;
	}

	
	private static HttpOptions currentOptions = new HttpOptions();
	public static HttpOptions options() {
		return currentOptions;
	}
	
	@Override
	public HttpOptions getOptions() {
		return currentOptions;
	}

	@Override
	public void setOptions(HttpOptions options) {
		currentOptions = options;		
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		return builder.page("Http", getOptions())
				.section("Proxy")
					.textInput("Proxy Url", HttpOptions::getProxyUrl, HttpOptions::setProxyUrl)
					.toggle("On 407: retry with prompted credentials", HttpOptions::isAskForProxyAuth, HttpOptions::setAskForProxyAuth)
				.endSection()
				.build();
	}
}
