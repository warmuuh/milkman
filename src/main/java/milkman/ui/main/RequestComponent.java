package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.ui.commands.UiCommand;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.Event;

@Singleton
public class RequestComponent {

	private final UiPluginManager plugins;
	@FXML TabPane tabs;
	@FXML HBox mainEditingArea;

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	private RequestContainer currentRequest; 
	
	
	@Inject
	public RequestComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}

	
	public void display(RequestContainer request) {
		this.currentRequest = request;
		RequestTypePlugin requestTypePlugin = plugins.loadRequestTypePlugins().get(0);
		mainEditingArea.getChildren().clear();
		RequestTypeEditor mainEditController = requestTypePlugin.getRequestEditor();
		mainEditingArea.getChildren().add(mainEditController.getRoot());
		mainEditController.displayRequest(request);
		
		tabs.getTabs().clear();
		for (RequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (RequestAspect aspect : request.getAspects()) {
				for (RequestAspectEditor tabController : plugin.getRequestTabs()) {
					if (tabController.canHandleAspect(aspect)) {
						Tab aspectTab = tabController.getRoot(aspect);
						aspectTab.setClosable(false);
						tabs.getTabs().add(aspectTab);
					}
				}
			}
		}
	}

	@FXML public void onSubmit() {
		onCommand.invoke(new UiCommand.SubmitRequest(currentRequest));
	}
	

	@FXML public void onSave() {
		onCommand.invoke(new UiCommand.SaveRequestCommand(currentRequest));
	}
	

	@FXML public void onSaveAs() {
		onCommand.invoke(new UiCommand.SaveRequestAsCommand(currentRequest));
	}

}
