package milkman.ui.plugin.rest.curl;

public class CurlExporter extends AbstractTextExporter {
	public CurlExporter() {
		super("Curl", new CurlTextExport());
	}
}
