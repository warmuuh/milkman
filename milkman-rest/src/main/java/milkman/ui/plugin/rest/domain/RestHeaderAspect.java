package milkman.ui.plugin.rest.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.HttpRequestBuilder;

@Data
public class RestHeaderAspect  extends RestRequestAspect  {

	private List<HeaderEntry> entries = new LinkedList<>();
	
	public RestHeaderAspect() {
		super("headers");
	}
	
	@Override
	public void enrichRequest(HttpRequestBuilder builder, Templater templater) throws Exception {
		entries.stream()
			.filter(HeaderEntry::isEnabled)
			.forEach(h -> builder.addHeader(templater.replaceTags(h.getName()), templater.replaceTags(h.getValue())));
	}
}
