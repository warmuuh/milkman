package milkman.utils.javafx.dnd;

import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;

import java.util.List;
import java.util.stream.Collectors;

public class FolderDndStrategy implements DndStrategy<Folder> {
    @Override
    public void drop(TreeView<Node> treeView, TreeItem<Node> newParent, TreeItem<Node> draggedItem, Folder droppedObject) {
        moveFolderToNewParent(treeView, newParent, draggedItem, droppedObject);
    }

    @Override
    public boolean isValidDropTarget(Object dropTarget) {
        return dropTarget instanceof Collection;
    }

    private void moveFolderToNewParent(TreeView<Node> treeView, TreeItem<Node> newParent, TreeItem<Node> draggedItem, Folder draggedFolder) {
        // remove from previous location
        var removedRequests = removeFromPreviousContainer(draggedFolder, draggedItem);

        addToNewLocation(treeView, newParent, draggedFolder, draggedItem, removedRequests);
        treeView.getSelectionModel().select(draggedItem);
    }

    private List<RequestContainer> removeFromPreviousContainer(Folder draggedFolder, TreeItem<Node> draggedItem) {
        TreeItem<Node> droppedItemParent = draggedItem.getParent();

        //if request was in a folder, remove ref from there
        if (droppedItemParent.getValue().getUserData() instanceof Folder) {
            Folder f = (Folder) droppedItemParent.getValue().getUserData();
            f.getFolders().remove(draggedFolder);
        } else if (droppedItemParent.getValue().getUserData() instanceof Collection) {
            Collection c = (Collection) droppedItemParent.getValue().getUserData();
            c.getFolders().remove(draggedFolder);
        }

        //remove from view
        getRootList(droppedItemParent.getChildren()).remove(draggedItem);

        //TODO: remove all contained requests from the collection
        //walk up until we find the collection
        Collection collection = findCollectionInParents(droppedItemParent);

        //remove from model
        var allContainedRequests = draggedFolder.getAllRequestsIncludingSubFolders();
        var requestsToBeRemoved = collection.getRequests().stream()
                .filter(req -> allContainedRequests.contains(req.getId()))
                .collect(Collectors.toList());
        collection.getRequests().removeAll(requestsToBeRemoved);
        return requestsToBeRemoved;
    }

    private void addToNewLocation(TreeView<Node> treeView,
                                  TreeItem<Node> dropTarget,
                                  Folder draggedFolder,
                                  TreeItem<Node> draggedItem,
                                  List<RequestContainer> requestsInFolder) {
        // dropping on collection makes it the first children
        if (dropTarget.getValue().getUserData() instanceof Collection) {
            Collection targetCollection = (Collection) dropTarget.getValue().getUserData();
            var folderOffset = targetCollection.getFolders().size();
            targetCollection.getFolders().add(draggedFolder);
            targetCollection.getRequests().addAll(folderOffset, requestsInFolder);
            getRootList(dropTarget.getChildren()).add(folderOffset, draggedItem);
            treeView.getSelectionModel().select(draggedItem);
        }
//        else if (dropTarget.getValue().getUserData() instanceof Folder) {
//            //dropping it onto folder makes it first request in folder
//            Folder f = (Folder) dropTarget.getValue().getUserData();
//            Collection collection = findCollectionInParents(dropTarget);
//            collection.getRequests().add(draggedRequest);
//            var folderOffset = f.getFolders().size();
//            f.getRequests().add(0, draggedRequest.getId());
//            getRootList(dropTarget.getChildren()).add(folderOffset, draggedItem);
//            treeView.getSelectionModel().select(draggedItem);
//        }
//        else {
//            // add to new location
//            int indexInParent = getRootList(dropTarget.getParent().getChildren()).indexOf(dropTarget);
//            getRootList(dropTarget.getParent().getChildren()).add(indexInParent + 1, draggedItem);
//
//            //dropped onto a request within a collection
//            if (dropTarget.getParent().getValue().getUserData() instanceof Collection) {
//                Collection thisCollection = (Collection) dropTarget.getParent().getValue().getUserData();
//                thisCollection.getRequests().add(indexInParent +1, draggedRequest);
//            } else if (dropTarget.getParent().getValue().getUserData() instanceof Folder) {
//                //dropped onto a request within a folder
//                Collection collection = findCollectionInParents(dropTarget);
//                collection.getRequests().add(draggedRequest);
//                Folder f = (Folder) dropTarget.getParent().getValue().getUserData();
//                var folderOffset = f.getFolders().size();
//                f.getRequests().add(indexInParent - folderOffset +1, draggedRequest.getId());
//            }
//        }
    }

    private Collection findCollectionInParents(TreeItem<Node> node) {
        Collection collection = null;
        while (collection == null || node == null) {
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
