package milkman.plugin.graphql;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.Value;
import milkman.plugin.graphql.domain.GraphqlAspect;
import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.JavaRequestProcessor;
import milkman.ui.plugin.rest.RequestProcessor;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.utils.AsyncResponseControl.AsyncControl;

public class GraphqlProcessor {
	RequestProcessor requestProcessor = new JavaRequestProcessor();
	
	
	@Value
	private static class GraphqlBody {
		String query;
		
		@JsonRawValue
		String variables;
	}
	
	public RestResponseContainer executeRequest(GraphqlRequestContainer request, Templater templater, AsyncControl asyncControl) {

		RestRequestContainer restContainer = toRestRequest(request);

		return requestProcessor.executeRequest(restContainer, templater, asyncControl);
	}

	public static RestRequestContainer toRestRequest(GraphqlRequestContainer request) {
		RestRequestContainer restContainer = new RestRequestContainer(request.getUrl(), "POST");
		request.getAspect(RestHeaderAspect.class).ifPresent(restContainer::addAspect);

		RestBodyAspect body = getBodyAspect(request);

		restContainer.addAspect(body);
		return restContainer;
	}


	private static RestBodyAspect getBodyAspect(GraphqlRequestContainer request) {
		GraphqlAspect gqlAspect = request.getAspect(GraphqlAspect.class).orElseThrow(() -> new IllegalArgumentException("Graphql Aspect not found"));
		
		String bodyStr = serializeGqlQuery(gqlAspect);
		var restBody = new RestBodyAspect();
		restBody.setBody(bodyStr);
		
		
		return restBody;
	}


	@SneakyThrows
	protected static String serializeGqlQuery(GraphqlAspect gqlAspect) {
		var mapper = new ObjectMapper();
		String bodyStr = mapper.writeValueAsString(new GraphqlBody(gqlAspect.getQuery(), gqlAspect.getVariables()));
		return bodyStr;
	}
	
}
