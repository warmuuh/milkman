package milkman.ui.plugin.rest.domain;

import lombok.Data;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.HttpRequestBuilder;
import org.apache.commons.lang3.StringUtils;

@Data
public class RestBodyAspect extends RestRequestAspect {

	String body;
	
	public RestBodyAspect() {
		super("body");
	}
	
	public void enrichRequest(HttpRequestBuilder builder, Templater templater) throws Exception {
		if (StringUtils.isNotBlank(body))
			builder.setBody(templater.replaceTags(body));
	}
}
