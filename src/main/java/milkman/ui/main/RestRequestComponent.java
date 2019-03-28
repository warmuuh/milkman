package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.MainEditingArea;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.TabController;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.UiRequestAspectsPlugin;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

@Singleton
public class RestRequestComponent {

	private final UiPluginManager plugins;
	@FXML TabPane tabs;
	private RestRequestContainer restRequest;
	@FXML HBox mainEditingArea;

	
	
	@Inject
	public RestRequestComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}

	
	public void display(RequestContainer request) {
		
		RequestTypePlugin requestTypePlugin = plugins.loadRequestTypePlugins().get(0);
		mainEditingArea.getChildren().clear();
		MainEditingArea mainEditController = requestTypePlugin.getMainEditingArea();
		mainEditingArea.getChildren().add(mainEditController.getRoot());
		mainEditController.displayRequest(request);
		
		tabs.getTabs().clear();
		for (UiRequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (TabController tabController : plugin.getRequestTabs()) {
				tabs.getTabs().add(tabController.getRoot());
			}
		}
	}
	
	@FXML public void onClick() {
		System.out.println(restRequest);
	}


}
