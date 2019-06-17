package milkman.ui.plugin.rest;

import lombok.Data;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class HttpOptionsPluginProvider implements OptionPageProvider<HttpOptionsPluginProvider.HttpOptions> {

	@Data
	public static class HttpOptions implements OptionsObject {
		private boolean useProxy = false;
		private String proxyUrl = "";
		private String proxyExclusion = "127.0.0.1|localhost";
		private boolean askForProxyAuth = false;
		private boolean certificateValidation = false;
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
					.toggle("Use Proxy", HttpOptions::isUseProxy, HttpOptions::setUseProxy)
					.toggle("On 407: retry with prompted credentials", HttpOptions::isAskForProxyAuth, HttpOptions::setAskForProxyAuth)
					.textInput("Proxy Url", HttpOptions::getProxyUrl, HttpOptions::setProxyUrl)
					.textInput("Exclude", HttpOptions::getProxyExclusion, HttpOptions::setProxyExclusion)
				.endSection()
				.section("Security")
					.toggle("Validate Certificates", HttpOptions::isCertificateValidation, HttpOptions::setCertificateValidation)
				.endSection()
				.build();
	}

	@Override
	public int getOrder() {
		return 200;
	}
	
	
}
