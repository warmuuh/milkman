package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;

@Singleton
public class ResponseComponent {

	private final UiPluginManager plugins;

	@FXML TabPane tabs;

	
	@Inject
	public ResponseComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}


	public void display(ResponseContainer response) {
		tabs.getTabs().clear();
		for (RequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (ResponseAspect aspect : response.getAspects()) {
				for (ResponseAspectEditor tabController : plugin.getResponseTabs()) {
					if (tabController.canHandleAspect(aspect))
						tabs.getTabs().add(tabController.getRoot(aspect));
				}
			}
		}
	}
}
