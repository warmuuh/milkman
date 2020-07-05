package milkman.plugin.graphql.export;

import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.ui.plugin.AbstractTextExporter;

public class HttpExporter extends AbstractTextExporter<GraphqlRequestContainer> {
    public HttpExporter() {
        super("Http", new HttpTextExport());
    }
}
