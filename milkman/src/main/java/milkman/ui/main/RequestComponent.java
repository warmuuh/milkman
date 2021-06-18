package milkman.ui.main;

import com.jfoenix.controls.JFXTabPane;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import lombok.Getter;
import milkman.ctrl.RequestTypeManager;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.*;
import milkman.ui.plugin.CustomCommand;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.FxmlBuilder.VboxExt;
import milkman.utils.javafx.JavaFxUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

import static milkman.utils.fxml.FxmlBuilder.hbox;

@Singleton
public class RequestComponent {

	 JFXTabPane tabs;
	 HBox mainEditingArea;
	 SplitMenuButton saveBtn;

	
	public final Event<UiCommand> onCommand = new Event<>();
	@Getter private RequestContainer currentRequest;
	 SplitMenuButton submitBtn;
	private final RequestTypeManager reqTypeManager;
	private final UiPluginManager plugins;
	
	
	@Inject
	public RequestComponent(UiPluginManager plugins, RequestTypeManager reqTypeManager) {
		this.plugins = plugins;
		this.reqTypeManager = reqTypeManager;
	}

	public void display(RequestContainer request, Optional<ResponseContainer> existingResponse) {
		this.currentRequest = request;
		mainEditingArea.getChildren().clear();
		RequestTypeEditor mainEditController = reqTypeManager.getRequestEditor(request);
		plugins.wireUp(mainEditController);
		mainEditingArea.getChildren().add(mainEditController.getRoot());
		mainEditController.displayRequest(request);
		int oldSelection = tabs.getSelectionModel().getSelectedIndex();

		tabs.getTabs().clear();
		
		submitBtn.getItems().clear();
		List<CustomCommand> customCommands = reqTypeManager.getPluginFor(currentRequest).getCustomCommands();
		customCommands.forEach(cc -> {
			MenuItem itm = new MenuItem(cc.getCommandText());
			itm.setOnAction(e -> onCommand.invoke(new SubmitCustomCommand(currentRequest, cc)));
			submitBtn.getItems().add(itm);
		});
		if (customCommands.isEmpty()) {
			submitBtn.getStyleClass().add("empty-menu");
		} else {
			submitBtn.getStyleClass().remove("empty-menu");
//			submitBtn.getStyleClass().clear();
		}
			
		plugins.loadRequestAspectPlugins().stream()
		.flatMap(p -> p.getRequestTabs().stream())
		.forEach(tabController -> {
			plugins.wireUp(tabController);
			if (tabController.canHandleAspect(request)) {
				Tab aspectTab = tabController.getRoot(request, existingResponse);
				aspectTab.setClosable(false);
				tabs.getTabs().add(aspectTab);
			}
		});

		tabs.getSelectionModel().select(oldSelection);
		
	}

	 public void onSubmit() {
		try {
			onCommand.invoke(new SubmitRequest(currentRequest));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	 public void onSave() {
		onCommand.invoke(new SaveRequestCommand(currentRequest));
	}
	

	 public void onSaveAs() {
		onCommand.invoke(new SaveRequestAsCommand(currentRequest));
	}


	 public void onExport() {
		onCommand.invoke(new ExportRequest(currentRequest));
	}

	
	public static class RequestComponentFxml extends VboxExt {
	
		public RequestComponentFxml(RequestComponent controller) {
			setId("requestComponent");
			HboxExt reqEdit = add(hbox("generalRequestEdit"));
			controller.mainEditingArea = reqEdit.add(hbox("mainEditingArea"), true);
			
			controller.submitBtn = reqEdit.add(new SplitMenuButton());

			JavaFxUtils.publishEscToParent(controller.submitBtn);
			controller.submitBtn.setId("submitBtn");
			controller.submitBtn.setText("submit");
			controller.submitBtn.setOnAction(e -> controller.onSubmit());
			
			
			controller.saveBtn = reqEdit.add(new SplitMenuButton());
			controller.saveBtn.setId("saveBtn");
			controller.saveBtn.setText("save");
			controller.saveBtn.setOnAction(e -> controller.onSave());
			controller.saveBtn.getStyleClass().add("secondary");
			
			MenuItem saveAs = new MenuItem("save as...");
			saveAs.setOnAction(e -> controller.onSaveAs());
			controller.saveBtn.getItems().add(saveAs);
			
			MenuItem export = new MenuItem("export...");
			export.setOnAction(e -> controller.onExport());
			controller.saveBtn.getItems().add(export);
			
			JFXTabPane tabPane = add(new JFXTabPane(), true);
			tabPane.setId("tabs");
			tabPane.setDisableAnimation(true);
			controller.tabs = tabPane;
			tabPane.setPrefHeight(300);
			
		}
	}
	
}
