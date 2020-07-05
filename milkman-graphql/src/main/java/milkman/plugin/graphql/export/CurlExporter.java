package milkman.plugin.graphql.export;

import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.ui.plugin.AbstractTextExporter;

public class CurlExporter extends AbstractTextExporter<GraphqlRequestContainer> {
    public CurlExporter() {
        super("Curl", new CurlTextExport());
    }
}
