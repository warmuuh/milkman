package milkman.ui.plugin.rest.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;

import lombok.Data;
import milkman.ui.plugin.Templater;

@Data
public class RestBodyAspect extends RestRequestAspect {

	String body;
	
	public void enrichRequest(RequestBuilder builder, Templater templater) throws Exception {
		if (StringUtils.isNotBlank(body))
			builder.setEntity(new StringEntity(templater.replaceTags(body)));
	}
}
