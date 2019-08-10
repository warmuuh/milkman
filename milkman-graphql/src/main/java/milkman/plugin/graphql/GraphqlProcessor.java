package milkman.plugin.graphql;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import milkman.plugin.graphql.domain.GraphqlAspect;
import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.RequestProcessor;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseContainer;

public class GraphqlProcessor {
	RequestProcessor requestProcessor = new RequestProcessor();
	
	
	@Value
	private static class GraphqlBody {
		String query;
		
		@JsonRawValue
		String variables;
	}
	
	public RestResponseContainer executeRequest(GraphqlRequestContainer request, Templater templater) {
		
		RestRequestContainer restContainer = new RestRequestContainer(request.getUrl(), "POST");
		request.getAspect(RestHeaderAspect.class).ifPresent(restContainer::addAspect);
		
		RestBodyAspect body = getBodyAspect(request);
		
		restContainer.addAspect(body);
		
		return requestProcessor.executeRequest(restContainer, templater);
	}


	private RestBodyAspect getBodyAspect(GraphqlRequestContainer request) {
		GraphqlAspect gqlAspect = request.getAspect(GraphqlAspect.class).orElseThrow(() -> new IllegalArgumentException("Graphql Aspect not found"));
		
		String bodyStr = serializeGqlQuery(gqlAspect);
		var restBody = new RestBodyAspect();
		restBody.setBody(bodyStr);
		
		
		return restBody;
	}


	@SneakyThrows
	protected String serializeGqlQuery(GraphqlAspect gqlAspect) {
		var mapper = new ObjectMapper();
		String bodyStr = mapper.writeValueAsString(new GraphqlBody(gqlAspect.getQuery(), gqlAspect.getVariables()));
		return bodyStr;
	}
	
}
