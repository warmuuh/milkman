package milkman.utils.javafx.dnd;

import static milkman.utils.javafx.DndUtil.JAVA_FORMAT;
import static milkman.utils.javafx.DndUtil.serialize;

import com.jfoenix.controls.JFXTreeCell;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;

public class DnDCellFactory implements Callback<TreeView<Node>, TreeCell<Node>> {
    private static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 0 0 2 0; -fx-padding: 3 3 1 3";
    private TreeCell<Node> dropZone;
    private TreeItem<Node> draggedItem;

    private final DndStrategy<RequestContainer> requestContainerDndStrategy = new RequestDndStrategy();
    private final DndStrategy<Folder> folderDndStrategyDndStrategy = new FolderDndStrategy();

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


            @Override
            protected void updateItem(Node item, boolean empty) {
                super.updateItem(item, empty);
                setDisclosureNode(null);
            }
        };
        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, treeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, treeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, treeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());
        cell.setDisclosureNode(null);
        return cell;
    }

    private void dragDetected(MouseEvent event, TreeCell<Node> treeCell, TreeView<Node> treeView) {
        draggedItem = treeCell.getTreeItem();

        // root can't be dragged
        if (draggedItem.getParent() == null) return;

        //only Requests and Folders can be dragged for now:
        if (!(draggedItem.getValue().getUserData() instanceof RequestContainer
                || draggedItem.getValue().getUserData() instanceof Folder)) {
            return;
        }

        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(JAVA_FORMAT, serialize(draggedItem.getValue().getUserData()));
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<Node> treeCell, TreeView<Node> treeView) {
        if (!event.getDragboard().hasContent(JAVA_FORMAT)) return;
        TreeItem<Node> dropTarget = treeCell.getTreeItem();

        boolean validDropTarget = draggedItem != null && dropTarget != null
                && !isSelfOrChildren(dropTarget, draggedItem) // can't drop on itself or any children
                && draggedItem.getParent() != null;

        // ignore if this is the root

        if (validDropTarget){
            if (draggedItem.getValue().getUserData() instanceof RequestContainer) {
                if (!requestContainerDndStrategy.isValidDropTarget(dropTarget.getValue().getUserData())) {
                    validDropTarget = false;
                }
            }
            if (draggedItem.getValue().getUserData() instanceof Folder) {
                if (!folderDndStrategyDndStrategy.isValidDropTarget(dropTarget.getValue().getUserData())) {
                    validDropTarget = false;
                }
            }
        }

        if (validDropTarget) {
            event.acceptTransferModes(TransferMode.MOVE);
            if (!Objects.equals(dropZone, treeCell)) {
                clearDropLocation();
                this.dropZone = treeCell;
                dropZone.setStyle(DROP_HINT_STYLE);
            }
        } else {
            clearDropLocation();
            this.dropZone = null;
        }

    }


    private boolean isSelfOrChildren(TreeItem<Node> dropTarget, TreeItem<Node> draggedItem) {
        if (dropTarget == draggedItem) {
            return true;
        }

        //is any of the children the droptarget?
        return draggedItem.getChildren().stream()
                .anyMatch(child -> isSelfOrChildren(dropTarget, child));
    }

    private void drop(DragEvent event, TreeCell<Node> treeCell, TreeView<Node> treeView) {

        try {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (!db.hasContent(JAVA_FORMAT)) return;

            TreeItem<Node> thisItem = treeCell.getTreeItem();
            var droppedObject = draggedItem.getValue().getUserData();
            if (droppedObject instanceof RequestContainer) {
                requestContainerDndStrategy.drop(treeView, thisItem, draggedItem, (RequestContainer) droppedObject);
            } else if (droppedObject instanceof Folder) {
                folderDndStrategyDndStrategy.drop(treeView, thisItem, draggedItem, (Folder) droppedObject);
            }
            event.setDropCompleted(success);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }

}
