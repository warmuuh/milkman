package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.ui.commands.UiCommand;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.Event;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;

@Singleton
public class RequestComponent {

	private final UiPluginManager plugins;
	@FXML JFXTabPane tabs;
	@FXML HBox mainEditingArea;

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	@Getter private RequestContainer currentRequest;
	@FXML JFXButton submitBtn; 
	
	
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
		try {
			onCommand.invoke(new UiCommand.SubmitRequest(currentRequest));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@FXML public void onSave() {
		onCommand.invoke(new UiCommand.SaveRequestCommand(currentRequest));
	}
	

	@FXML public void onSaveAs() {
		onCommand.invoke(new UiCommand.SaveRequestAsCommand(currentRequest));
	}

}
