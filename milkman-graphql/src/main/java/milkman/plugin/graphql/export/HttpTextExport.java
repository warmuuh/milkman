package milkman.plugin.graphql.export;

import milkman.plugin.graphql.GraphqlProcessor;
import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TextExport;

public class HttpTextExport implements TextExport<GraphqlRequestContainer> {

    private final milkman.ui.plugin.rest.curl.HttpTextExport restHttp = new milkman.ui.plugin.rest.curl.HttpTextExport();

    @Override
    public String export(boolean isWindows, GraphqlRequestContainer request, Templater templater) {
        return restHttp.export(isWindows, GraphqlProcessor.toRestRequest(request), templater);
    }
}
