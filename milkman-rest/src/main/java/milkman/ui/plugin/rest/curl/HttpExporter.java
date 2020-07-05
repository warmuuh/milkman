package milkman.ui.plugin.rest.curl;

import milkman.ui.plugin.AbstractTextExporter;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class HttpExporter extends AbstractTextExporter<RestRequestContainer> {
	public HttpExporter() {
		super("Http", new HttpTextExport());
	}
}
