package milkman.ui.plugin.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.ui.plugin.MainEditingArea;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.TabController;
import milkman.ui.plugin.UiRequestAspectsPlugin;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class RestPlugin implements UiRequestAspectsPlugin, RequestTypePlugin {

	@Override
	public List<TabController> getRequestTabs() {
		return Arrays.asList(new RequestHeaderTabController(), new RequestBodyTabController());
	}

	@Override
	public List<TabController> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public RequestContainer createNewRequest() {
		return new RestRequestContainer("todos", "https://jsonplaceholder.typicode.com/todos", "GET");
	}

	@Override
	public MainEditingArea getMainEditingArea() {
		return new RestRequestEditController();
	}

	
}
