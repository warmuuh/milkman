package milkman.plugin.graphql;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.graphql.domain.GraphqlAspect;
import milkman.plugin.graphql.domain.GraphqlRequestContainer;
import milkman.plugin.graphql.editor.GraphqlAspectEditor;
import milkman.plugin.graphql.editor.GraphqlRequestEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.AsyncResponseControl.AsyncControl;

public class GraphqlPlugin implements RequestAspectsPlugin, RequestTypePlugin {

	GraphqlProcessor requestProcessor = new GraphqlProcessor();
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new GraphqlAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof GraphqlRequestContainer) {
			if (!request.getAspect(GraphqlAspect.class).isPresent())
				request.addAspect(new GraphqlAspect());
			
			if (!request.getAspect(RestHeaderAspect.class).isPresent()) {
				var headers = new RestHeaderAspect();
				headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), "Content-Type", "application/json", true));
				request.addAspect(headers);
			}

		}
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		

	}

	@Override
	public int getOrder() {
		return 15;
	}

	@Override
	public RequestContainer createNewRequest() {
		return new GraphqlRequestContainer("New Graphql Request", "");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new GraphqlRequestEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater,
			AsyncControl asyncControl) {
		return requestProcessor.executeRequest((GraphqlRequestContainer) request, templater, asyncControl);

	}
	
	@Override
	public String getRequestType() {
		return "GraphQl";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof GraphqlRequestContainer;
	}

	
}
