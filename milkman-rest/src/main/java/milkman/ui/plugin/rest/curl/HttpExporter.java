package milkman.ui.plugin.rest.curl;

public class HttpExporter extends AbstractTextExporter {
	public HttpExporter() {
		super("Http", new HttpTextExport());
	}
}
