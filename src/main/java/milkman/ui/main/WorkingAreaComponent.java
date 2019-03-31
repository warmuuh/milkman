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
		openedRequests.forEach(r -> openedTabsBar.getChildren().add(createRequestTabButton(r, activeRequest == r)));
		restRequestComponent.display(activeRequest);
	}

	private Button createRequestTabButton(RequestContainer r, boolean isActive) {
		String btnName = (isActive ? ">" : "") + r.getName();
		btnName += r.isDirty() ? "*" : "";
		
		Button button = new Button(btnName);
		
		
		r.onDirtyChange.add((o,n) -> {
			String text = button.getText();
			if (n) 
				button.setText(text + "*");
			else
				button.setText(text.substring(0, text.length()-1));
		});
		
		
		button.setOnAction(e -> onCommand.invoke(new UiCommand.SwitchToRequest(r)));
		
		MenuItem closeEntry = new MenuItem("Close");
		closeEntry.setOnAction(a -> onCommand.invoke(new UiCommand.CloseRequest(r)));
		
		MenuItem renameEntry = new MenuItem("Rename");
		renameEntry.setOnAction(a -> onCommand.invoke(new UiCommand.RenameRequest(r, false)));
		
		button.setContextMenu(new ContextMenu(closeEntry, renameEntry));
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
