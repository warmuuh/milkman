package milkman.ui.main;

import com.jfoenix.controls.JFXTabPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.*;
import milkman.ui.commands.UiCommand.CloseRequest.CloseType;
import milkman.ui.main.RequestComponent.RequestComponentFxml;
import milkman.ui.main.ResponseComponent.ResponseComponentFxml;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.utils.AsyncResponseControl;
import milkman.utils.Event;
import milkman.utils.fxml.FxmlBuilder.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

import static milkman.utils.fxml.FxmlBuilder.*;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Slf4j
public class WorkingAreaComponent {

	private static final String CSS_CLASS_REQUEST_ACTIVE = "mm-request-active";
	private static final String CSS_CLASS_REQUEST_DIRTY = "mm-request-dirty";
	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	@Getter
	private Parent requestArea;

	private SplitPane splitPane;

	JFXTabPane tabPane;
	private ChangeListener<? super Tab> tabChangeListener;
	
	public void display(RequestContainer activeRequest, List<RequestContainer> openedRequests, Optional<ResponseContainer> existingResponse) {
		restRequestComponent.display(activeRequest, existingResponse);
		setupTabs(activeRequest, openedRequests);
		log.info("request area refreshed");
	}

	public void toggleLayout(boolean horizontalLayout) {
		CoreApplicationOptionsProvider.options().setHorizontalLayout(horizontalLayout);
		splitPane.setOrientation(horizontalLayout ? Orientation.HORIZONTAL : Orientation.VERTICAL);
	}

	private void setupTabs(RequestContainer activeRequest, List<RequestContainer> openedRequests) {
		tabPane.getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
		tabPane.getTabs().clear();
		Tab activeTab = null;
		for(RequestContainer r : openedRequests){
			boolean isActive = activeRequest == r;
			Tab tab = createTestTab(r, isActive);
			tabPane.getTabs().add(tab);
			if (isActive)
				activeTab = tab;
		}
		if (activeTab != null) {
			Tab t = activeTab;
//			Platform.runLater(() -> {
				tabPane.getSelectionModel().select(t);	
//			});
		}
		
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
		tabPane.requestLayout();
		log.info("tab repainted");
	}
	private Tab createTestTab(RequestContainer r, boolean isActive) {
		Tab tab = new Tab();

		tab.setGraphic(getTitleNode(r));

		if (isActive) {
			tab.getStyleClass().add(CSS_CLASS_REQUEST_ACTIVE);
		}

		r.onDirtyChange.add((o,n) -> Platform.runLater(
			() -> tab.setGraphic(getTitleNode(r))
		));
		
		MenuItem closeEntry = new MenuItem("Close");
		closeEntry.setOnAction(a -> onCommand.invoke(new CloseRequest(r, CloseType.CLOSE_THIS)));
		
		MenuItem closeRightEntry = new MenuItem("Close Tabs to the Right");
		closeRightEntry.setOnAction(a -> onCommand.invoke(new CloseRequest(r, CloseType.CLOSE_RIGHT)));
		
		MenuItem closeAllEntry = new MenuItem("Close All");
		closeAllEntry.setOnAction(a -> onCommand.invoke(new CloseRequest(r, CloseType.CLOSE_ALL)));
		

		MenuItem closeOtherEntry = new MenuItem("Close Others");
		closeOtherEntry.setOnAction(a -> onCommand.invoke(new CloseRequest(r, CloseType.CLOSE_OTHERS)));
		
		MenuItem renameEntry = new MenuItem("Rename");
		renameEntry.setOnAction(a -> {
			onCommand.invoke(new RenameRequest(r));
		});


		MenuItem duplicateEntry = new MenuItem("Duplicate");
		duplicateEntry.setOnAction(a -> {
			onCommand.invoke(new DuplicateRequest(r));
		});

		tab.setContextMenu(new ContextMenu(closeEntry, closeRightEntry,closeAllEntry, closeOtherEntry, renameEntry, duplicateEntry));
		tab.setUserData(r);

		tab.getGraphic().setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.MIDDLE) {
				onCommand.invoke(new CloseRequest(r, CloseType.CLOSE_THIS));
				e.consume();
			}
		});
		
		return tab;
	}

	private HBox getTitleNode(RequestContainer r) {
		var title = new HBox();
		title.getChildren().add(new Label(r.getName()));
		if (r.isDirty()){
			var dot = new Circle(4.0);
			dot.getStyleClass().add(CSS_CLASS_REQUEST_DIRTY);
			title.getChildren().add(dot);
		}
		return title;
	}

	public void displayResponseFor(RequestContainer request, AsyncResponseControl response) {
		//only, if the current request should be shown.
		//should prevent background-tabs to be rendered, if response arrives.
		if (restRequestComponent.getCurrentRequest().getId().equals(request.getId())) {
			responseComponent.display(request, response);
		}
	}
	
	 public void onNewRequestClick() {
		onCommand.invoke(new NewRequest(tabPane));
	}

	public void clearResponse() {
		hideSpinner();
		responseComponent.clear();
	}
	
	public void showSpinner(Runnable cancellationListener) {
		responseComponent.showSpinner(cancellationListener);
	}
	public void hideSpinner() {
		responseComponent.hideSpinner();
	}
	
	

	public void initialize() {
		tabChangeListener = (obs, o, n) -> {
			if (n != null && o != n) {
				RequestContainer r = (RequestContainer) n.getUserData();
				onCommand.invoke(new SwitchToRequest(r));
			}
		};
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
		//Bug in jfoenix: when not disabling this, the GUI does not refresh except if i hover over it with mouse
		//might be because we dont really use the content of the tabs
		tabPane.setDisableAnimation(true);
	}
	
	public static class WorkingAreaComponentFxml extends VboxExt {
		public WorkingAreaComponentFxml(WorkingAreaComponent controller) {
			setId("working-area");
			HboxExt reqArea = add(hbox("openRequestArea"));
			
			JFXTabPane pane = new JFXTabPane();
			pane.setId("tabPane");
			controller.tabPane = reqArea.add(pane, true);
			
			
			reqArea.add(button(icon(FontAwesomeIcon.PLUS), controller::onNewRequestClick))
				.setPrefWidth(40);
			
			
			
			SplitPane splitPane = add(new SplitPane(), true);
			controller.splitPane = splitPane;
			var orientation = CoreApplicationOptionsProvider.options().isHorizontalLayout()
					? Orientation.HORIZONTAL
					: Orientation.VERTICAL;
			splitPane.setOrientation(orientation);

			controller.requestArea = new RequestComponentFxml(controller.restRequestComponent);
			splitPane.getItems().add(controller.requestArea);
			splitPane.getItems().add(new ResponseComponentFxml(controller.responseComponent));
			
			
			controller.initialize();
		}
	}
	
}
