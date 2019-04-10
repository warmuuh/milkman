package milkman.ui.plugin.rest;

import java.util.Arrays;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class RestPlugin implements RequestAspectsPlugin, RequestTypePlugin {

	RequestProcessor requestProcessor = new RequestProcessor();
	
	
	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Arrays.asList(new RequestHeaderTabController(), new RequestBodyTabController());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Arrays.asList(new ResponseHeaderTabController(), new ResponseBodyTabController());
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
	public void initializeAspects(RequestContainer request) {
		if (!request.hasAspect(RestHeaderAspect.class))
			request.addAspect(new RestHeaderAspect());
		
		if (!request.hasAspect(RestBodyAspect.class))
			request.addAspect(new RestBodyAspect());
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		return requestProcessor.executeRequest((RestRequestContainer) request, templater);
	}

	@Override
	public void initializeAspects(ResponseContainer response) {
		// we dont need to do anything here as we created the request (where we added everything already)
	}

	
}
