package milkman.utils.javafx;

import javafx.scene.Node;
import javafx.scene.Parent;

public class JavaFxUtils {

    public static boolean isParent(Parent parent, Node child) {
        if (child == null) {
            return false;
        }
        Parent curr = child.getParent();
        while (curr != null) {
            if (curr == parent) {
                return true;
            }
            curr = curr.getParent();
        }
        return false;
    }
}
