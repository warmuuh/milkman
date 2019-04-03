package milkman.ui.main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.jfoenix.controls.JFXListView;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.val;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.ui.commands.UiCommand;
import milkman.utils.Event;

@Singleton
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class RequestCollectionComponent {

	public final Event<UiCommand> onCommand = new Event<UiCommand>();

	@FXML
	JFXListView<JFXListView<Node>> collectionContainer;
	
	@FXML
	TextField searchField;

	Map<String, Boolean> expansionCache = new HashMap<>();

	public void display(List<Collection> collections) {

		List<JFXListView<Node>> entries = new LinkedList<JFXListView<Node>>();
		for (Collection collection : collections) {
			JFXListView<Node> subList = new JFXListView<Node>();
			
			subList.setGroupnode(createCollectionEntry(collection));
			subList.setUserData(collection);
			
			//TODO: this does not work. JFXListView is not firing expandedPropertyChange, bug?
			subList.setExpanded(expansionCache.getOrDefault(collection.getName(), false));
			subList.expandedProperty().addListener((v, o, n) -> expansionCache.put(collection.getName(), n));
			
			List<Node> requestNodes = collection.getRequests().stream()
				.map(r -> createRequestEntry(collection, r))
				.collect(Collectors.toList());
			
			subList.setItems(FXCollections.observableList(requestNodes));
			entries.add(subList);
		}
		
		val filteredList = new FilteredList<>(FXCollections.observableList(entries, c -> 
				((Collection)c.getUserData()).getRequests().stream()
				.map(r -> r.onDirtyChange)
				.collect(Collectors.toList())
				.toArray(new Observable[]{}))
		);
		setFilterPredicate(filteredList, searchField.getText());
		collectionContainer.setItems(filteredList);
		searchField.textProperty().addListener((obs) -> 
			setFilterPredicate(filteredList, searchField.getText())
		);
	}

	private HBox createCollectionEntry(Collection collection) {
		return new HBox(new FontAwesomeIconView(FontAwesomeIcon.FOLDER_ALT, "1.5em"), new Label(collection.getName()));
	}

	private void setFilterPredicate(FilteredList<JFXListView<Node>> filteredList,
			String searchTerm) {
		if (searchTerm != null && searchTerm.length() > 0)
			filteredList.setPredicate(o -> ((Collection)o.getUserData()).match(searchTerm));
		else
			filteredList.setPredicate(o -> true);
	}


	
	private Node createRequestEntry(Collection collection, RequestContainer request) {
		Label button = new Label(request.getName());

		MenuItem renameEntry = new MenuItem("Rename");
		renameEntry.setOnAction(e -> onCommand.invoke(new UiCommand.RenameRequest(request, true)));

		MenuItem deleteEntry = new MenuItem("Delete");
		deleteEntry.setOnAction(e -> onCommand.invoke(new UiCommand.DeleteRequest(request, collection)));

		ContextMenu ctxMenu = new ContextMenu(renameEntry, deleteEntry);
		VBox vBox = new VBox(button);
		
		vBox.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY)
				onCommand.invoke(new UiCommand.LoadRequest(request.getId()));
			else if (e.getButton() == MouseButton.SECONDARY)
				ctxMenu.show(vBox, e.getScreenX(), e.getScreenY());
				
		});
		return vBox;
	}

	@FXML public void clearSearch() {
		searchField.clear();
	}

}
