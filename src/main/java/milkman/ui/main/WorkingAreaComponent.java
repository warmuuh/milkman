package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import milkman.domain.RequestContainer;
import milkman.utils.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class WorkingAreaComponent {

	private final RestRequestComponent restRequestComponent;
	private final RestResponseComponent responseComponent;
	@FXML ToolBar openedTabsBar;

	
	public void display(RequestContainer activeRequest) {
		openedTabsBar.getItems().clear();
		openedTabsBar.getItems().add(new Button(activeRequest.getName()));
		restRequestComponent.display(activeRequest);
		responseComponent.display(activeRequest);
	}
	
}
