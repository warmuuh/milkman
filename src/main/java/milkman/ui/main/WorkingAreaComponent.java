package milkman.ui.main;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.utils.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class WorkingAreaComponent {

	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;
	@FXML ToolBar openedTabsBar;

	
	public final Event<Void> onNewRequest = new Event<Void>();
	public final Event<RequestContainer> onRequestSelection = new Event<RequestContainer>();
	
	public void display(RequestContainer activeRequest, List<RequestContainer> openedRequests) {
		openedTabsBar.getItems().clear();
		openedRequests.forEach(r -> {
			Button button = new Button(r.getName());
			button.setOnAction(e -> onRequestSelection.invoke(r));
			openedTabsBar.getItems().add(button);	
		});
		restRequestComponent.display(activeRequest);
	}
	
	public void display(ResponseContainer activeResponse) {
		responseComponent.display(activeResponse);
	}
	
	@FXML public void onNewRequestClick() {
		onNewRequest.invoke(null);
	}

	public void clearResponse() {
		responseComponent.clear();
	}
}
