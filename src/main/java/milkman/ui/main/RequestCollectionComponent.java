package milkman.ui.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import milkman.ctrl.WorkspaceController;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.ui.commands.UiCommand;
import milkman.utils.Event;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class RequestCollectionComponent {

	
	public final Event<UiCommand> onCommand = new Event<UiCommand>();

	
	@FXML VBox collectionContainer;

	Map<String, Boolean> expansionCache = new HashMap<>();
	
	public void display(List<Collection> collections) {
		collectionContainer.getChildren().clear();
		for (Collection collection : collections) {
			TitledPane pane = createPane(collection);
			collectionContainer.getChildren().add(pane);
		}
	}


	private TitledPane createPane(Collection collection) {
		List<Node> entries = collection.getRequests().stream().map(r -> createRequestEntry(collection, r)).collect(Collectors.toList());
		TitledPane titledPane = new TitledPane(collection.getName(), new VBox(entries.toArray(new Node[] {})));
		titledPane.setExpanded(expansionCache.getOrDefault(collection.getName(), false));
		titledPane.expandedProperty().addListener((v, o, n)->expansionCache.put(collection.getName(), n));
		return titledPane;
	}


	private Node createRequestEntry(Collection collection, RequestContainer request) {
		Button button = new Button(request.getName());
		button.setOnAction(e -> onCommand.invoke(new UiCommand.LoadRequest(request.getId())));
		
		MenuItem renameEntry = new MenuItem("Rename");
		renameEntry.setOnAction(e -> onCommand.invoke(new UiCommand.RenameRequest(request, true)));

		MenuItem deleteEntry = new MenuItem("Delete");
		deleteEntry.setOnAction(e -> onCommand.invoke(new UiCommand.DeleteRequest(request, collection)));
		
		button.setContextMenu(new ContextMenu(renameEntry, deleteEntry));
		return button;
	}
	
}
