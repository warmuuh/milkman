package milkman.ui.plugin.rest.domain;

import org.apache.http.client.methods.RequestBuilder;

import milkman.domain.RequestAspect;
import milkman.ui.plugin.Templater;

public abstract class RestRequestAspect extends RequestAspect {

	public abstract void enrichRequest(RequestBuilder builder, Templater templater) throws Exception;
}
