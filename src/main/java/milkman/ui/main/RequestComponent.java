package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;


import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.utils.Event;

@Singleton
public class RequestComponent {

	private final UiPluginManager plugins;
	@FXML TabPane tabs;
	private RestRequestContainer restRequest;
	@FXML HBox mainEditingArea;

	
	public final Event<RequestContainer> onRequestSubmit = new Event<RequestContainer>(); 
	
	
	@Inject
	public RequestComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}

	
	public void display(RequestContainer request) {
		
		RequestTypePlugin requestTypePlugin = plugins.loadRequestTypePlugins().get(0);
		mainEditingArea.getChildren().clear();
		RequestTypeEditor mainEditController = requestTypePlugin.getRequestEditor();
		mainEditingArea.getChildren().add(mainEditController.getRoot());
		mainEditController.displayRequest(request);
		mainEditController.onSubmit(() -> onRequestSubmit.invoke(request));
		
		tabs.getTabs().clear();
		for (RequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (RequestAspect aspect : request.getAspects()) {
				for (RequestAspectEditor tabController : plugin.getRequestTabs()) {
					if (tabController.canHandleAspect(aspect))
						tabs.getTabs().add(tabController.getRoot(aspect));
				}
			}
		}
	}
	


}
