package milkman.ui.main;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import milkman.ctrl.RequestTypeManager;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.ui.commands.UiCommand;
import milkman.ui.plugin.ContentTypeAwareEditor;
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

	@FXML JFXTabPane tabs;
	@FXML HBox mainEditingArea;

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	@Getter private RequestContainer currentRequest;
	@FXML JFXButton submitBtn;
	private RequestTypeManager reqTypeManager;
	private UiPluginManager plugins; 
	
	
	@Inject
	public RequestComponent(UiPluginManager plugins, RequestTypeManager reqTypeManager) {
		this.plugins = plugins;
		this.reqTypeManager = reqTypeManager;
	}

	
	public void display(RequestContainer request) {
		this.currentRequest = request;
		mainEditingArea.getChildren().clear();
		RequestTypeEditor mainEditController = reqTypeManager.getRequestEditor(request);
		mainEditingArea.getChildren().add(mainEditController.getRoot());
		mainEditController.displayRequest(request);
		int oldSelection = tabs.getSelectionModel().getSelectedIndex();
		
		tabs.getTabs().clear();
		
		plugins.loadRequestAspectPlugins().stream()
		.flatMap(p -> p.getRequestTabs().stream())
		.forEach(tabController -> {
			if (tabController.canHandleAspect(request)) {
				if (tabController instanceof ContentTypeAwareEditor) {
					((ContentTypeAwareEditor) tabController).setContentTypePlugins(plugins.loadContentTypePlugins());
				}
				Tab aspectTab = tabController.getRoot(request);
				aspectTab.setClosable(false);
				tabs.getTabs().add(aspectTab);
			}
		});

		tabs.getSelectionModel().select(oldSelection);
		
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


	@FXML public void onExport() {
		onCommand.invoke(new UiCommand.ExportRequest(currentRequest));
	}

}
