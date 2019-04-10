package milkman.ui.main;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jfoenix.controls.JFXTabPane;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.UiPluginManager;

@Singleton
public class ResponseComponent implements Initializable {

	private final UiPluginManager plugins;

	@FXML JFXTabPane tabs;

	@FXML Node spinner;

	@FXML HBox statusDisplay;

	
	@Inject
	public ResponseComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}


	public void display(RequestContainer request, ResponseContainer response) {
		clear();
		hideSpinner();

		addStatusInformation(response.getStatusInformations());
		
		for (RequestAspectsPlugin plugin : plugins.loadRequestAspectPlugins()) {
			for (ResponseAspectEditor tabController : plugin.getResponseTabs()) {
				if (tabController.canHandleAspect(request, response)) {
					if (tabController instanceof ContentTypeAwareEditor) {
						((ContentTypeAwareEditor) tabController).setContentTypePlugins(plugins.loadContentTypePlugins());
					}
					Tab aspectTab = tabController.getRoot(request, response);
					aspectTab.setClosable(false);
					tabs.getTabs().add(aspectTab);
				}
			}
		}
	}


	private void addStatusInformation(Map<String, String> statusInformations) {
		statusDisplay.getChildren().clear();
		for (Entry<String, String> entry : statusInformations.entrySet()) {
			Label name = new Label(entry.getKey() + ":");
			Label value = new Label(entry.getValue());
			value.getStyleClass().add("emphasized");
			statusDisplay.getChildren().add(new HBox(name, value));
		}
	}


	public void clear() {
		statusDisplay.getChildren().clear();
		tabs.getTabs().clear();
	}
	
	public void showSpinner() {
		statusDisplay.getChildren().clear();
		spinner.setVisible(true);
	}


	public void hideSpinner() {
		spinner.setVisible(false);
		
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		hideSpinner();
	}
}
