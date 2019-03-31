package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.UiPluginManager;

@Singleton
public class ResponseComponent {

	private final UiPluginManager plugins;

	@FXML TabPane tabs;

	
	@Inject
	public ResponseComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}


	public void display(ResponseContainer response) {
		clear();
		for (RequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (ResponseAspect aspect : response.getAspects()) {
				for (ResponseAspectEditor tabController : plugin.getResponseTabs()) {
					if (tabController.canHandleAspect(aspect)) {
						Tab aspectTab = tabController.getRoot(aspect);
						aspectTab.setClosable(false);
						tabs.getTabs().add(aspectTab);
					}
				}
			}
		}
	}


	public void clear() {
		tabs.getTabs().clear();
	}
}
