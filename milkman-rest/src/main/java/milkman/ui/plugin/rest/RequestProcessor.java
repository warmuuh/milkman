package milkman.ui.plugin.rest;

import lombok.SneakyThrows;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.utils.AsyncResponseControl.AsyncControl;

public interface RequestProcessor {

	RestResponseContainer executeRequest(RestRequestContainer request, Templater templater, AsyncControl asyncControl);

}