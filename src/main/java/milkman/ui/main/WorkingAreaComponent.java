package milkman.ui.main;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.commands.UiCommand;
import milkman.utils.Event;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class WorkingAreaComponent {

	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;
	@FXML Pane openedTabsBar;

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	
	public void display(RequestContainer activeRequest, List<RequestContainer> openedRequests) {
		openedTabsBar.getChildren().clear();
		openedRequests.forEach(r -> openedTabsBar.getChildren().add(createRequestTabButton(r)));
		restRequestComponent.display(activeRequest);
	}

	private Button createRequestTabButton(RequestContainer r) {
		Button button = new Button(r.getName());
		button.setOnAction(e -> onCommand.invoke(new UiCommand.SwitchToRequest(r)));
		MenuItem closeEntry = new MenuItem("Close");
		closeEntry.setOnAction(a -> onCommand.invoke(new UiCommand.CloseRequest(r)));
		button.setContextMenu(new ContextMenu(closeEntry));
		return button;
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
}
