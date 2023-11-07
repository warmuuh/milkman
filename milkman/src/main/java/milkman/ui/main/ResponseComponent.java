package milkman.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.jfoenix.controls.JFXTabPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import milkman.domain.RequestContainer;
import milkman.domain.StatusInfoContainer;
import milkman.domain.StatusInfoContainer.StatusEntry;
import milkman.ui.components.FancySpinner;
import milkman.ui.components.StatusPopup;
import milkman.ui.components.TinySpinner;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.AsyncResponseControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import milkman.utils.CollectionUtils;

import static milkman.utils.fxml.FxmlBuilder.*;

@Singleton
public class ResponseComponent {

	private final UiPluginManager plugins;

	 JFXTabPane tabs;

	 Node spinner;

	 HBox statusDisplay;

	 JFXButton cancellation;
	
	 TinySpinner asyncControlSpinner;

	private int oldSelection = -1;

	
	@Inject
	public ResponseComponent(UiPluginManager plugins) {
		this.plugins = plugins;
	}


	public void display(RequestContainer request, AsyncResponseControl respCtrl) {
		var response = respCtrl.getResponse();
		
		clear();
		hideSpinner();

		addStatusInformation(response.getStatusInformations());
		
		setupAsyncControl(respCtrl);
		
		plugins.loadRequestAspectPlugins().stream()
			.flatMap(p -> p.getResponseTabs().stream())
			.forEach(tabController -> {
				plugins.wireUp(tabController);
				if (tabController.canHandleAspect(request, response)) {
					Tab aspectTab = tabController.getRoot(request, response);
					aspectTab.setClosable(false);
					tabs.getTabs().add(aspectTab);
				}
			});
		
		tabs.getSelectionModel().select(oldSelection);
	}


	private void setupAsyncControl(AsyncResponseControl respCtrl) {
		CompletableFuture<Object> reqCompleted = CompletableFuture.anyOf(respCtrl.onRequestFailed, respCtrl.onRequestSucceeded);
		asyncControlSpinner.setVisible(!reqCompleted.isDone());
		if (!reqCompleted.isDone()) {
			reqCompleted.thenRun(() -> asyncControlSpinner.setVisible(false));
			asyncControlSpinner.setOnAction(e -> respCtrl.cancleRequest());
		}
		
	}


	private void addStatusInformation(StatusInfoContainer statusInformations) {
		statusDisplay.getChildren().clear();
		statusInformations.subscribe(entry ->  Platform.runLater(() -> {

			int idx = CollectionUtils.indexOfFirst(statusDisplay.getChildren(), e -> entry.getKey().equals(e.getUserData()));
			Optional<Node> existingNode = idx < 0 ? Optional.empty() : Optional.of(statusDisplay.getChildren().get(idx));
			HBox node = renderStatusInfo(entry, existingNode);
			node.setUserData(entry.getKey());

			if (idx < 0) {
				statusDisplay.getChildren().add(node);
				FXCollections.sort(statusDisplay.getChildren(),
						Comparator.comparing(n -> ((Parent)n).getChildrenUnmodifiable().size())
								.thenComparing(n -> (Comparable)((Node)n).getUserData()));
			} else {
				statusDisplay.getChildren().set(idx, node);
			}
		}));
	}

	private HBox renderStatusInfo(StatusEntry entry, Optional<Node> nodeToBeReplaced) {
		if (entry.isGroup()) {
			Label name = new Label(entry.getKey());
			name.setUnderline(true);

			Map<String, String> values = nodeToBeReplaced.map(n -> (Map<String, String>) ((HBox)n).getChildren().get(0).getUserData())
							.orElse(new LinkedHashMap<>());
			values.putAll(entry.getValueMap());
			name.setUserData(values); //bit hacky: we store the key-map in this field to be able to merge later

			HBox hBox = new HBox(name);
			hBox.setStyle("-fx-cursor: hand");
			hBox.setOnMouseClicked(e -> {
				new StatusPopup(values).show(hBox, PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0, 20);
			});
			return hBox;
		} else {
			Label name = new Label(entry.getKey() + ":");
			Label value = new Label(entry.getValue().getText());
			if (!CoreApplicationOptionsProvider.options().isDisableColorfulUi()) {
				entry.getValue().getStyle().ifPresent(value::setStyle);
			}
			value.getStyleClass().add("emphasized");
			HBox node = new HBox(name, value);
			return node;
		}
	}

	public void clear() {
		if (tabs.getSelectionModel().getSelectedIndex() > -1)
			oldSelection = tabs.getSelectionModel().getSelectedIndex();
		statusDisplay.getChildren().clear();
		tabs.getTabs().clear();
	}
	
	public void showSpinner(Runnable cancellationListener) {
		statusDisplay.getChildren().clear();
		cancellation.setVisible(true);
		cancellation.setOnAction(e -> cancellationListener.run());
		spinner.setVisible(true);
	}


	public void hideSpinner() {
		cancellation.setVisible(false);
		spinner.setVisible(false);
		asyncControlSpinner.setVisible(false);
	}


	public void initialize() {
		hideSpinner();
	}
	
	
	public static class ResponseComponentFxml extends VboxExt {
		
		public ResponseComponentFxml(ResponseComponent controller) {
			getStyleClass().add("responseArea");
			
			var stackPane = add(new StackPane(), true);
			
			AnchorPane anchorPane = new AnchorPane();
			stackPane.getChildren().add(anchorPane);
			
			JFXTabPane tabPane = anchorNode(new JFXTabPane(), 1.0, 1.0, 1.0, 1.0);
			tabPane.setDisableAnimation(true);
			controller.tabs = tabPane;
			tabPane.setId("tabs");
			anchorPane.getChildren().add(tabPane);

			
			var statusBar = anchorNode(hbox("statusBar"), 3.0, 5.0, null, null);
			
			controller.statusDisplay = hbox("statusDisplay");
			statusBar.add(controller.statusDisplay);
			
			var asyncSpinner = controller.asyncControlSpinner = new TinySpinner();
			asyncSpinner.setVisible(false);
			statusBar.add(asyncSpinner, true);
			
			
			anchorPane.getChildren().add(statusBar);
			
			
			
			var spinner = new FancySpinner();
			controller.spinner = spinner;
			StackPane.setAlignment(spinner, Pos.CENTER);
			stackPane.getChildren().add(spinner);
			

			var cancel = button("cancellation", icon(FontAwesomeIcon.TIMES));
			controller.cancellation = cancel;
			StackPane.setAlignment(cancel, Pos.CENTER);
			stackPane.getChildren().add(cancel);
			
			
			controller.initialize();
		}
	}
	
}
