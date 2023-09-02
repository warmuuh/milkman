package milkman.ui.plugin.rest;

import java.util.List;
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
		private boolean followRedirects = false;
		private String httpProtocol = "HTTP/2";

		public boolean isHttp2Support() {
			return httpProtocol.contains("HTTP/2");
		}

		public boolean isHttp3Support() {
			return httpProtocol.contains("HTTP/3");
		}
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
				.section("Requests")
					.toggle("Validate Certificates", HttpOptions::isCertificateValidation, HttpOptions::setCertificateValidation)
					.toggle("Follow Redirects", HttpOptions::isFollowRedirects, HttpOptions::setFollowRedirects)
				.selection("Protocol", HttpOptions::getHttpProtocol, HttpOptions::setHttpProtocol, List.of("HTTP/1.1", "HTTP/2", "HTTP/3"))
				.endSection()
				.build();
	}

	@Override
	public int getOrder() {
		return 200;
	}
	
	
}
