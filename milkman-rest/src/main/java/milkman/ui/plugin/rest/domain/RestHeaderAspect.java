package milkman.ui.plugin.rest.domain;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.methods.RequestBuilder;

import lombok.Data;
import milkman.ui.plugin.Templater;

@Data
public class RestHeaderAspect  extends RestRequestAspect  {

	private List<HeaderEntry> entries = new LinkedList<>();
	
	@Override
	public void enrichRequest(RequestBuilder builder, Templater templater) throws Exception {
		entries.stream()
			.filter(HeaderEntry::isEnabled)
			.forEach(h -> builder.addHeader(templater.replaceTags(h.getName()), templater.replaceTags(h.getValue())));
	}
}
