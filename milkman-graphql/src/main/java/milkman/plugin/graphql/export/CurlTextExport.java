package milkman.plugin.graphql.export;

import milkman.plugin.graphql.GraphqlProcessor;
import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TextExport;

public class CurlTextExport implements TextExport<GraphqlRequestContainer> {

    private final milkman.ui.plugin.rest.curl.CurlTextExport restCurl = new milkman.ui.plugin.rest.curl.CurlTextExport();

    @Override
    public String export(boolean isWindows, GraphqlRequestContainer request, Templater templater) {
        return restCurl.export(isWindows, GraphqlProcessor.toRestRequest(request), templater);
    }
}
