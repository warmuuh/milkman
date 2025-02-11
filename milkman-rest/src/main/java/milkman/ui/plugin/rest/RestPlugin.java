package milkman.ui.plugin.rest;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.*;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestQueryParamAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.utils.AsyncResponseControl.AsyncControl;

import java.util.Arrays;
import java.util.List;

public class RestPlugin implements RequestAspectsPlugin, RequestTypePlugin {


	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Arrays.asList(new RequestHeaderTabController(), new RequestBodyTabController(), new RequestQueryParamTabController());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Arrays.asList( new ResponseBodyTabController(),
				new BinaryResponseBodyTabController(),
				new ResponseHeaderTabController(),
				new DebugRequestHeaderTabController(),
				new DebugRequestBodyTabController());
	}

	@Override
	public RequestContainer createNewRequest() {
		return new RestRequestContainer("New Request", "", "GET");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new RestRequestEditController();
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof RestRequestContainer) {
			if (!request.getAspect(RestHeaderAspect.class).isPresent())
				request.addAspect(new RestHeaderAspect());

			if (!request.getAspect(RestBodyAspect.class).isPresent())
				request.addAspect(new RestBodyAspect());
			
			if (!request.getAspect(RestQueryParamAspect.class).isPresent()) {
				RestQueryParamAspect aspect = new RestQueryParamAspect();
				aspect.parseQueryParams(((RestRequestContainer) request).getUrl());
				request.addAspect(aspect);
			}
		}
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater, AsyncControl asyncControl) {
		RequestProcessor requestProcessor = new JettyRequestProcessor();
		return requestProcessor.executeRequest((RestRequestContainer) request, templater, asyncControl);
		
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		// we dont need to do anything here as we created the request (where we added everything already)
	}

	@Override
	public String getRequestType() {
		return "HTTP";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof RestRequestContainer;
	}

	@Override
	public int getOrder() {
		return 10;
	}

	
	
	
}
