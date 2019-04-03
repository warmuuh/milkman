package milkman.ui.main;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.commands.UiCommand;
import milkman.utils.Event;
import com.jfoenix.controls.JFXTabPane;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class WorkingAreaComponent implements Initializable {

	private static final String CSS_CLASS_REQUEST_ACTIVE = "mm-request-active";
	private static final String CSS_CLASS_REQUEST_DIRTY = "mm-request-dirty";
	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	@FXML JFXTabPane tabPane;
	private ChangeListener<? super Tab> tabChangeListener;
	
	public void display(RequestContainer activeRequest, List<RequestContainer> openedRequests) {
		setupTabs(activeRequest, openedRequests);
		restRequestComponent.display(activeRequest);
	}

	private void setupTabs(RequestContainer activeRequest, List<RequestContainer> openedRequests) {
		tabPane.getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
		tabPane.getTabs().clear();
		openedRequests.forEach(r -> {
			boolean isActive = activeRequest == r;
			Tab tab = createTestTab(r, isActive);
			tabPane.getTabs().add(tab);
			if (isActive)
				tabPane.getSelectionModel().select(tab);
		});
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);


	}
	private Tab createTestTab(RequestContainer r, boolean isActive) {
		Tab tab = new Tab(r.getName());
		if (isActive)
			tab.getStyleClass().add(CSS_CLASS_REQUEST_ACTIVE);
		
		if (r.isDirty())
			tab.getStyleClass().add(CSS_CLASS_REQUEST_DIRTY);
		
		r.onDirtyChange.add((o,n) -> {
			if (n) 
				tab.getStyleClass().add(CSS_CLASS_REQUEST_DIRTY);
			else
				tab.getStyleClass().remove(CSS_CLASS_REQUEST_DIRTY);
		});
		
		MenuItem closeEntry = new MenuItem("Close");
		closeEntry.setOnAction(a -> onCommand.invoke(new UiCommand.CloseRequest(r)));
		
		MenuItem renameEntry = new MenuItem("Rename");
		renameEntry.setOnAction(a -> onCommand.invoke(new UiCommand.RenameRequest(r, false)));
		
		tab.setContextMenu(new ContextMenu(closeEntry, renameEntry));
		
		tab.setUserData(r);

		
		return tab;
	}

	public void display(ResponseContainer activeResponse) {
		responseComponent.display(activeResponse);
	}
	
	@FXML public void onNewRequestClick() {
		onCommand.invoke(new UiCommand.NewRequest());
	}

	public void clearResponse() {
		responseComponent.clear();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tabChangeListener = (obs, o, n) -> {
			if (n != null && o != n) {
				RequestContainer r = (RequestContainer) n.getUserData();
				onCommand.invoke(new UiCommand.SwitchToRequest(r));
			}
		};
		tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
	}
}
