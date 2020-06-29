package milkman.ui.plugin.rest.curl;

import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public interface TextExport {

    public String export(boolean isWindows, RestRequestContainer container, Templater templater);
}
