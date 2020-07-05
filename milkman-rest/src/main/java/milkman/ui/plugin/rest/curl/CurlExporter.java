package milkman.ui.plugin.rest.curl;

import milkman.ui.plugin.AbstractTextExporter;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class CurlExporter extends AbstractTextExporter<RestRequestContainer> {
	public CurlExporter() {
		super("Curl", new CurlTextExport());
	}
}
