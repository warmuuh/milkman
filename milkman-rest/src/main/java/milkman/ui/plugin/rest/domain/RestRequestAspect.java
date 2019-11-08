package milkman.ui.plugin.rest.domain;

import milkman.domain.RequestAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.HttpRequestBuilder;

public abstract class RestRequestAspect extends RequestAspect {

	public abstract void enrichRequest(HttpRequestBuilder builder, Templater templater) throws Exception;
	
	public RestRequestAspect(String name) {
		super(name);
	}
}
