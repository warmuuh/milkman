package milkman.utils.javafx.dnd;

import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;

public class RequestDndStrategy implements DndStrategy<RequestContainer> {
    @Override
    public void drop(TreeView<Node> treeView,
                     TreeItem<Node> newParent,
                     TreeItem<Node> draggedItem,
                     RequestContainer droppedObject) {
        moveRequestToNewParent(treeView, newParent, draggedItem, droppedObject);
    }

    private void moveRequestToNewParent(TreeView<Node> treeView,
                                        TreeItem<Node> newParent,
                                        TreeItem<Node> draggedItem,
                                        RequestContainer droppedObject) {
        RequestContainer draggedRequest = droppedObject;
        // remove from previous location
        removeFromPreviousContainer(draggedRequest, draggedItem);

        addToNewLocation(treeView, newParent, draggedRequest, draggedItem);
        treeView.getSelectionModel().select(draggedItem);
    }

    private void removeFromPreviousContainer(RequestContainer draggedRequest, TreeItem<Node> draggedItem) {
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

    private void addToNewLocation(TreeView<Node> treeView,
                                  TreeItem<Node> dropTarget,
                                  RequestContainer draggedRequest,
                                  TreeItem<Node> draggedItem) {
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


    private ObservableList<TreeItem<Node>> getRootList(ObservableList<TreeItem<Node>> children) {
        if (children instanceof TransformationList)
            return getRootList(((TransformationList) children).getSource());
        return children;
    }
}
