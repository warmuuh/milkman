package milkman.ui.plugin.rest.cli;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import milkman.domain.ResponseAspect;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkmancli.AspectCliPresenter;

public class BodyResponseCliPresenter implements AspectCliPresenter {

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseBodyAspect;
	}

	@Override
	public String getStringRepresentation(ResponseAspect aspect) {
		RestResponseBodyAspect body = (RestResponseBodyAspect) aspect;
		if (CoreApplicationOptionsProvider.options().isAutoformatContent()) {
			return tryFormat(body.getBody());
		}
		return body.getBody();
	}

	protected String tryFormat(String body) {
		// TODO: need to check format first, i.e. application/json, but we dont have
		// headers here, so lets just try to format json for now
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			JsonNode jsonNode = mapper.readTree(body);
			return mapper.writeValueAsString(jsonNode);
		} catch (IOException e) {
			/* did not work, just return normal format */
		}
		return body;
	}

	@Override
	public int getOrder() {
		return 20;
	}
}
