package milkman.utils.javafx.dnd;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public interface DndStrategy<T> {
    void drop(TreeView<Node> treeView,
              TreeItem<Node> newParent,
              TreeItem<Node> draggedItem,
              T droppedObject);

}
