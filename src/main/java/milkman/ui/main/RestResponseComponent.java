package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.TabController;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.UiRequestAspectsPlugin;

@Singleton
public class RestResponseComponent {

	private final UiPluginManager plugins;

	@FXML TabPane tabs;

	
	@Inject
	public RestResponseComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}


	public void display(RequestContainer request) {
		tabs.getTabs().clear();
		for (UiRequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (TabController tabController : plugin.getRequestTabs()) {
				tabs.getTabs().add(tabController.getRoot());
			}
		}
	}
}
