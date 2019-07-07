package milkman.utils.javafx;
import java.util.HashMap;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jfoenix.controls.JFXTreeCell;

import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import lombok.SneakyThrows;
import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.persistence.UnknownPluginHandler;

public class DnDCellFactory implements Callback<TreeView<Node>, TreeCell<Node>> {
    private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");
    private static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 0 0 2 0; -fx-padding: 3 3 1 3";
    private TreeCell<Node> dropZone;
    private TreeItem<Node> draggedItem;

    @Override
    public TreeCell<Node> call(TreeView<Node> treeView) {
        TreeCell<Node> cell = new JFXTreeCell<Node>() {
//            @Override
//            protected void updateItem(Node item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null) return;
//
//                ImageView iv1 = new ImageView();
//                if (item.getName().equals("Tasks")) {
//                    iv1.setImage(TASKS_IMAGE);
//                }
//                else {
//                    iv1.setImage(PIN_IMAGE);
//                }
//                setGraphic(iv1);
//                setText(item.getName());
//            }
        };
        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, treeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, treeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, treeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());
        
        return cell;
    }

    private void dragDetected(MouseEvent event, TreeCell<Node> treeCell, TreeView<Node> treeView) {
        draggedItem = treeCell.getTreeItem();

        // root can't be dragged
        if (draggedItem.getParent() == null) return;
        
        //only Requests can be dragged for now:
        if (!(draggedItem.getValue().getUserData() instanceof RequestContainer))
        	return;
        
        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(JAVA_FORMAT, serialize(draggedItem.getValue().getUserData()));
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<Node> treeCell, TreeView<Node> treeView) {
        if (!event.getDragboard().hasContent(JAVA_FORMAT)) return;
        TreeItem<Node> thisItem = treeCell.getTreeItem();

        // can't drop on itself
        if (draggedItem == null || thisItem == null || thisItem == draggedItem) return;
        // ignore if this is the root
        if (draggedItem.getParent() == null) {
            clearDropLocation();
            return;
        }

        event.acceptTransferModes(TransferMode.MOVE);
        if (!Objects.equals(dropZone, treeCell)) {
            clearDropLocation();
            this.dropZone = treeCell;
            dropZone.setStyle(DROP_HINT_STYLE);
        }
    }

    private void drop(DragEvent event, TreeCell<Node> treeCell, TreeView<Node> treeView) {
    	
        try {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (!db.hasContent(JAVA_FORMAT)) return;

			TreeItem<Node> thisItem = treeCell.getTreeItem();
			RequestContainer draggedRequest = (RequestContainer) draggedItem.getValue().getUserData();
			// remove from previous location
			removeFromPreviousContainer(draggedRequest);
			
			addToNewLocation(treeView, thisItem, draggedRequest);
			treeView.getSelectionModel().select(draggedItem);
			event.setDropCompleted(success);
		} catch (Throwable t) {
			t.printStackTrace();
		}
    }


	private void removeFromPreviousContainer(RequestContainer draggedRequest) {
		TreeItem<Node> droppedItemParent = draggedItem.getParent();
		
		//if request was in a folder, remove ref from there
		if (droppedItemParent.getValue().getUserData() instanceof Folder) {
			Folder f = (Folder) droppedItemParent.getValue().getUserData();
			f.getRequests().remove(draggedRequest.getId());
		}
		
		//remove from view
		getRootList(droppedItemParent.getChildren()).remove(draggedItem);
		
		//walk up until we find the collection
		Collection collection = findCollectionInParents(droppedItemParent);
	
		//remove from model
		collection.getRequests().remove(draggedRequest);
		
	}

	private Collection findCollectionInParents(TreeItem<Node> node) {
		Collection collection = null;
		while(collection == null || node == null) {
			if (node.getValue().getUserData() instanceof Collection) {
				collection = (Collection) node.getValue().getUserData();
			}
			node = node.getParent();
		}
		return collection;
	}
	
	
	private void addToNewLocation(TreeView<Node> treeView, TreeItem<Node> dropTarget, RequestContainer draggedRequest) {
		// dropping on collection makes it the first children
		if (dropTarget.getValue().getUserData() instanceof Collection) {
			Collection targetCollection = (Collection) dropTarget.getValue().getUserData();
			var folderOffset = targetCollection.getFolders().size();
			targetCollection.getRequests().add(folderOffset, draggedRequest);
			getRootList(dropTarget.getChildren()).add(folderOffset, draggedItem);
			treeView.getSelectionModel().select(draggedItem);
		} else if (dropTarget.getValue().getUserData() instanceof Folder) {
			//dropping it onto folder makes it first request in folder
			Folder f = (Folder) dropTarget.getValue().getUserData();
			Collection collection = findCollectionInParents(dropTarget);
			collection.getRequests().add(draggedRequest);
			var folderOffset = f.getFolders().size();
			f.getRequests().add(0, draggedRequest.getId());
			getRootList(dropTarget.getChildren()).add(folderOffset, draggedItem);
			treeView.getSelectionModel().select(draggedItem);
		}
		else {
		    // add to new location
		    int indexInParent = getRootList(dropTarget.getParent().getChildren()).indexOf(dropTarget);
		    getRootList(dropTarget.getParent().getChildren()).add(indexInParent + 1, draggedItem);
		    
		    //dropped onto a request within a collection
		    if (dropTarget.getParent().getValue().getUserData() instanceof Collection) {
		    	Collection thisCollection = (Collection) dropTarget.getParent().getValue().getUserData();
		    	thisCollection.getRequests().add(indexInParent +1, draggedRequest);
		    } else if (dropTarget.getParent().getValue().getUserData() instanceof Folder) {
			    //dropped onto a request within a folder
				Collection collection = findCollectionInParents(dropTarget);
				collection.getRequests().add(draggedRequest);
				Folder f = (Folder) dropTarget.getParent().getValue().getUserData();
				var folderOffset = f.getFolders().size();
				f.getRequests().add(indexInParent - folderOffset +1, draggedRequest.getId());
		    }
		}
	}

    private ObservableList<TreeItem<Node>> getRootList(ObservableList<TreeItem<Node>> children) {
    	if (children instanceof TransformationList)
    		return getRootList(((TransformationList) children).getSource());
    	return children;
	}

	private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }

    @SneakyThrows
    private String serialize(Object obj) {
    	return createMapper().writeValueAsString(obj);
    }
    
    @SneakyThrows
    private <T> T deserialize(String content, Class<T> type) {
    	return createMapper().readValue(content, type);
    }
    
    private ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		return mapper;
	}
}