package milkman.ui.main;

import static milkman.utils.fxml.facade.FxmlBuilder.VboxExt;
import static milkman.utils.fxml.facade.FxmlBuilder.button;
import static milkman.utils.fxml.facade.FxmlBuilder.hbox;
import static milkman.utils.fxml.facade.FxmlBuilder.icon;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.text;
import static milkman.utils.fxml.facade.FxmlBuilder.treeView;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.domain.Searchable;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.AddFolder;
import milkman.ui.commands.UiCommand.DeleteCollection;
import milkman.ui.commands.UiCommand.DeleteFolder;
import milkman.ui.commands.UiCommand.DeleteRequest;
import milkman.ui.commands.UiCommand.ExportCollection;
import milkman.ui.commands.UiCommand.ExportRequest;
import milkman.ui.commands.UiCommand.LoadRequest;
import milkman.ui.commands.UiCommand.RenameCollection;
import milkman.ui.commands.UiCommand.RenameRequest;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.utils.Event;
import milkman.utils.PropertyChangeEvent;
import milkman.utils.javafx.SettableTreeItem;
import milkman.utils.javafx.dnd.DnDCellFactory;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.reactfx.EventStreams;

@Singleton
@Slf4j
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class RequestCollectionComponent {

	public final Event<UiCommand> onCommand = new Event<UiCommand>();


	TreeView<Node> collectionContainer;
	

	TextField searchField;

	Map<String, Boolean> expansionCache = new HashMap<>();

	private SettableTreeItem<Node> root;
	
	public void display(List<Collection> collections) {

		collectionContainer.setShowRoot(false);
		collectionContainer.setCellFactory(new DnDCellFactory());
		List<TreeItem<Node>> entries = new LinkedList<>();
		for (Collection collection : collections) {
			entries.add(buildTree(collection));
		}

		
		ObservableList<TreeItem<Node>> observableList = FXCollections.observableList(entries, c -> 
			{
				Collection collection = (Collection)c.getValue().getUserData();
				List<PropertyChangeEvent<Boolean>> reqObs = collection.getRequests().stream()
					.map(r -> r.onDirtyChange)
					.collect(Collectors.toList());
				reqObs.add(collection.onDirtyChange);
				return reqObs.toArray(new Observable[]{});
			});
		
		
		SortedList<TreeItem<Node>> sortedList = observableList.sorted((a,b) -> {
				val ac = (Collection)a.getValue().getUserData();
				val bc = (Collection)b.getValue().getUserData();
				return new CompareToBuilder()
					.append(!ac.isStarred(), !bc.isStarred())
					.append(ac.getName().toLowerCase(), bc.getName().toLowerCase())
					.build();
			});
		
		
		
		val filteredList = new FilteredList<>(sortedList);
		
		root = new SettableTreeItem<Node>();
		
		root.setChildren(filteredList);
		Platform.runLater(() -> setFilterPredicate(filteredList, searchField.getText()));
		collectionContainer.setRoot(root);
		
		EventStreams.nonNullValuesOf(searchField.textProperty())
			.successionEnds(Duration.ofMillis(250))
			.subscribe(qry -> setFilterPredicate(filteredList, qry));

	}
	
	
	public TreeItem<Node> buildTree(Collection collection){
		SettableTreeItem<Node> item = new SettableTreeItem<Node>();
		HBox collEntry = createCollectionEntry(collection, item);
		item.setValue(collEntry);
		item.getValue().setUserData(collection);
		item.setExpanded(expansionCache.getOrDefault(collection.getId(), false));
		item.expandedProperty().addListener((obs, o, n) -> {
			expansionCache.put(collection.getId(), n);
		});

		
		List<RequestContainer> collectionRequests = new LinkedList<>(collection.getRequests());
		
		List<TreeItem<Node>> children = new LinkedList<TreeItem<Node>>();
		for (Folder f : collection.getFolders()) {
			SettableTreeItem<Node> folderItm = buildTreeForFolder(collection, f, collectionRequests);
			children.add(folderItm);
		}
		
		
		for (RequestContainer r : collectionRequests) {
			TreeItem<Node> requestTreeItem = new TreeItem<Node>(createRequestEntry(collection, r));
			requestTreeItem.getValue().setUserData(r);
			children.add(requestTreeItem);
		}
		item.setChildren(new FilteredList<TreeItem<Node>>(FXCollections.observableList(children)));
		return item;
	}


	/**
	 * builds tree for folders and removes contained requests from the collectionRequest list
	 */
	private SettableTreeItem<Node> buildTreeForFolder(Collection collection, Folder f, List<RequestContainer> collectionRequests) {
		SettableTreeItem<Node> folderItm = new SettableTreeItem<Node>();
		folderItm.setValue(createFolderEntry(f, collection, folderItm));
		folderItm.getValue().setUserData(f);
		folderItm.setExpanded(expansionCache.getOrDefault(f.getId(), false));
		folderItm.expandedProperty().addListener((obs, o, n) -> {
			expansionCache.put(f.getId(), n);
		});

		
//		List<TreeItem<Node>> children = new LinkedList<TreeItem<Node>>();
		for (Folder childFolder : f.getFolders()) {
			var childFolderNode = buildTreeForFolder(collection, childFolder, collectionRequests);
//			children.add(childFolderNode);
			folderItm.getChildren().add(childFolderNode);
		}
		for (String reqId : f.getRequests()) {
			RequestContainer req = collectionRequests.stream().filter(r -> r.getId().equals(reqId)).findAny().orElse(null);
			if (req != null) {
				TreeItem<Node> requestTreeItem = new TreeItem<Node>(createRequestEntry(collection, req));
				requestTreeItem.getValue().setUserData(req);
//				children.add(requestTreeItem);
				folderItm.getChildren().add(requestTreeItem);
				collectionRequests.remove(req);
			}
		}
		
		//TODO: cannot set children on folders for some reason as they will get GC'ed instantly!??!
		//that means, we can't use FilteredList properly and search in folders
		//does not work that nicely
//		folderItm.setChildren(new FilteredList<TreeItem<Node>>(FXCollections.observableList(children)));

		return folderItm;
	}
	

	private HBox createCollectionEntry(Collection collection, TreeItem<Node> item) {
		Label collectionName = new Label(collection.getName());
		HBox.setHgrow(collectionName, Priority.ALWAYS);
		
		
		
		var starringBtn = button();
		starringBtn.getStyleClass().add("btn-starring");
		starringBtn.setGraphic(collection.isStarred() ? new FontAwesomeIconView(FontAwesomeIcon.STAR, "1em") : new FontAwesomeIconView(FontAwesomeIcon.STAR_ALT, "1em"));
		starringBtn.setOnAction( e -> {
			collection.setStarred(!collection.isStarred());
			//HACK invoke directly bc we want to trigger sorting (see further up).
			//this does not change the dirty-state as the 
			//dirty state in collection is not used any further
			collection.onDirtyChange.invoke(false, true);   

			starringBtn.setGraphic(collection.isStarred() ? new FontAwesomeIconView(FontAwesomeIcon.STAR, "1em") : new FontAwesomeIconView(FontAwesomeIcon.STAR_ALT, "1em"));
		});
		
		
		HBox hBox = new HBox(new FontAwesomeIconView(FontAwesomeIcon.FOLDER_ALT, "1.5em"), collectionName, starringBtn);

		
		hBox.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				item.setExpanded(!item.isExpanded());
				e.consume();
			}
			if (e.getButton() == MouseButton.SECONDARY) {
				collectionCtxMenu.show(collection, hBox, e.getScreenX(), e.getScreenY());
				e.consume();
			}
		});
		return hBox;
	}

	private void setFilterPredicate(FilteredList<TreeItem<Node>> filteredList,
			String searchTerm) {
		if (searchTerm != null && searchTerm.length() > 0)
			filteredList.setPredicate(o -> {
				Object userData = o.getValue().getUserData();
				if (userData instanceof Collection) {
					Collection collection = (Collection) userData;
					if (collection.isStarred() || collection.match(searchTerm)) {
						((FilteredList<TreeItem<Node>>)o.getChildren()).setPredicate(ro -> true);
						return true;
					}
					
					((FilteredList<TreeItem<Node>>)o.getChildren()).setPredicate(ro -> {
						if (ro.getValue().getUserData() instanceof Folder) {
							//as we cant use filtered lists in folders (See line 193)
							//we have to do cheap version here and check all contaiend folders&requests, if they
							//contain a matching request and show the whole folder
							return findMatchingRequestInFolderStructure(ro, searchTerm);
						} else if (ro.getValue().getUserData() instanceof RequestContainer) {
							RequestContainer req = (RequestContainer) ro.getValue().getUserData();
							return req.match(searchTerm);
						}
						return false;
					});
					
					return o.getChildren().size() > 0;
				}
				return false;
			});
		else {
			filteredList.setPredicate(o -> {
				Object userData = o.getValue().getUserData();
				if (userData instanceof Collection) {
					((FilteredList<TreeItem<Node>>)o.getChildren()).setPredicate(ro -> true);
				}
				return true;
			});
			
			
		}
	}


	
	private boolean findMatchingRequestInFolderStructure(TreeItem<Node> ro, String searchTerm) {
		if (ro.getValue().getUserData() instanceof Searchable) {
			Searchable s = (Searchable) ro.getValue().getUserData();
			if (s.match(searchTerm))
				return true;
		}
		
		return ro.getChildren().stream()
				.anyMatch(c -> findMatchingRequestInFolderStructure(c, searchTerm));
	}


	private Node createRequestEntry(Collection collection, RequestContainer request) {


		Node requestType = getRequestDescriptor(request, CoreApplicationOptionsProvider.options().isDisableColorfulUi());
		Label button = new Label(request.getName());

		VBox vBox = new VBox(new HBox(requestType,button));
		vBox.getStyleClass().add("request-entry");
		vBox.setOnMouseClicked(e -> {
//			if (e.getButton() == MouseButton.PRIMARY)
//				onCommand.invoke(new UiCommand.LoadRequest(request.getId()));
//			else
			if (e.getButton() == MouseButton.PRIMARY) {
				onCommand.invoke(new LoadRequest(request.getId()));
				e.consume();
			}
			if (e.getButton() == MouseButton.SECONDARY) {
				reqCtxMenu.show(request, collection, vBox, e.getScreenX(), e.getScreenY());
				e.consume();
			}
				
		});
		return vBox;
	}

	private Node getRequestDescriptor(RequestContainer request, boolean disableColorfulUi) {
		var reqTypeDesc = request.getTypeDescriptor();

		if (disableColorfulUi) {
			Label requestType = new Label(reqTypeDesc.getName());
			requestType.getStyleClass().add("request-type");
			return requestType;
		}

		if (reqTypeDesc.getIconFile().isPresent()) {
			var url = reqTypeDesc.getIconFile().get();
			try(InputStream is = url.openStream()){
				return new ImageView(new Image(is, 50, 20, true, true));
			} catch (IOException e) {
				log.error("Failed to load image {}", url);
			}
		}

		Label requestType = new Label(reqTypeDesc.getName());
		requestType.getStyleClass().add("request-type");
		reqTypeDesc.getFxStyle().ifPresent(requestType::setStyle);
		return requestType;
	}

	private Node createFolderEntry(Folder folder, Collection collection, TreeItem<Node> item) {
		Label folderName = new Label(folder.getName());
		HBox.setHgrow(folderName, Priority.ALWAYS);
		
		
		HBox hBox = new HBox(new FontAwesomeIconView(FontAwesomeIcon.FOLDER, "1.5em"), folderName);
		hBox.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				item.setExpanded(!item.isExpanded());
				e.consume();
			}
			if (e.getButton() == MouseButton.SECONDARY) {
				folderCtxMenu.show(folder, collection, hBox, e.getScreenX(), e.getScreenY());
				e.consume();
			}
		});
		return hBox;
	}


	public void clearSearch() {
		searchField.clear();
	}

	public void selectRequest(RequestContainer request) {
		findTreeItemOfRequest(request, root)
				.ifPresent(n -> collectionContainer.getSelectionModel().select(n));
	}

	private Optional<TreeItem<Node>> findTreeItemOfRequest(RequestContainer curRequest, TreeItem<Node> curNode) {
		if (curNode.getValue() != null && curNode.getValue().getUserData() != null){
			var userData = curNode.getValue().getUserData();
			if (userData instanceof RequestContainer && ((RequestContainer) userData).getId().equals(curRequest.getId())) {
				return Optional.of(curNode);
			}
		}

		for (TreeItem<Node> child : curNode.getChildren()) {
			var childResult = findTreeItemOfRequest(curRequest, child);
			if (childResult.isPresent()) {
				return childResult;
			}
		}

		return Optional.empty();
	}

	/**
	 * we create single instances for ctx menu to be reused for all requests/collections/folders, bc creation of context menu is expensive
	 * @author peter
	 *
	 */
	@Data
	public class CollectionContextMenu extends ContextMenu {
		Collection collection;
		public CollectionContextMenu() {
			MenuItem addFolderEntry = new MenuItem("Add Folder...");
			addFolderEntry.setOnAction(e -> onCommand.invoke(new AddFolder(collection)));
			getItems().add(addFolderEntry);

			MenuItem renameEntry = new MenuItem("Rename");
			renameEntry.setOnAction(e -> onCommand.invoke(new RenameCollection(collection)));
			getItems().add(renameEntry);

			
			MenuItem exportEntry = new MenuItem("Export");
			exportEntry.setOnAction(e -> onCommand.invoke(new ExportCollection(collection)));
			getItems().add(exportEntry);

			MenuItem deleteEntry = new MenuItem("Delete");
			deleteEntry.setOnAction(e -> onCommand.invoke(new DeleteCollection(collection)));
			getItems().add(deleteEntry);

			
		}
		
		public void show(Collection collection, Node anchor, double screenX, double screenY) {
			this.collection = collection;
			super.show(anchor, screenX, screenY);
		}
	}
	private final CollectionContextMenu collectionCtxMenu = new CollectionContextMenu();

	/**
	 * we create two single instances for ctx menu to be reused for all requests/collections, bc creation of context menu is expensive
	 * @author peter
	 *
	 */
	@Data
	public class RequestContextMenu extends ContextMenu {
		RequestContainer request;
		Collection collection;
		public RequestContextMenu() {
			MenuItem renameEntry = new MenuItem("Rename");
			renameEntry.setOnAction(e -> onCommand.invoke(new RenameRequest(request)));
			getItems().add(renameEntry);
			
			MenuItem exportEntry = new MenuItem("Export");
			exportEntry.setOnAction(e -> onCommand.invoke(new ExportRequest(request)));
			getItems().add(exportEntry);
			
			MenuItem deleteEntry = new MenuItem("Delete");
			deleteEntry.setOnAction(e -> onCommand.invoke(new DeleteRequest(request, collection)));
			getItems().add(deleteEntry);
		}
		
		public void show(RequestContainer request, Collection collection, Node anchor, double screenX, double screenY) {
			this.request = request;
			this.collection = collection;
			super.show(anchor, screenX, screenY);
		}
	}
	private final RequestContextMenu reqCtxMenu = new RequestContextMenu();
	
	
	
	@Data
	public class FolderContextMenu extends ContextMenu {
		Folder folder;
		Collection collection;
		public FolderContextMenu() {

			MenuItem addFolderEntry = new MenuItem("Add Folder");
			addFolderEntry.setOnAction(e -> onCommand.invoke(new AddFolder(folder)));
			getItems().add(addFolderEntry);
			
			MenuItem deleteEntry = new MenuItem("Delete");
			deleteEntry.setOnAction(e -> onCommand.invoke(new DeleteFolder(collection, folder)));
			getItems().add(deleteEntry);
		}
		
		public void show(Folder folder, Collection collection, Node anchor, double screenX, double screenY) {
			this.folder = folder;
			this.collection = collection;
			super.show(anchor, screenX, screenY);
		}
	}
	private final FolderContextMenu folderCtxMenu = new FolderContextMenu();

	
	
	public static class RequestCollectionComponentFxml extends VboxExt {
		
		public RequestCollectionComponentFxml(RequestCollectionComponent controller) {
			setId("request-collection");
			setMinWidth(200);
			setMaxWidth(400);
			
			var searchArea = add(hbox("searchArea"));
			
			var searchAreaField = searchArea.add(hbox("searchAreaField"), true);
			
			searchAreaField.add(label(icon(FontAwesomeIcon.SEARCH)));
			
			controller.searchField = searchAreaField.add(text("searchField", "search..."), true);
			
			searchAreaField.add(button(icon(FontAwesomeIcon.TIMES_CIRCLE_ALT), controller::clearSearch));
			
			controller.collectionContainer = treeView();
			controller.collectionContainer.setId("collectionContainer");
			add(controller.collectionContainer, true);
			
		}
	}
	
	
}
