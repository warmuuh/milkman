package milkman.utils.javafx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

    public static void publishEscToParent(Node node){
        node.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            //propagate to parent so that dialog closes correctly / request actually stops
            if (e.getCode() == KeyCode.ESCAPE){
                var newEvt = e.copyFor(e.getSource(), node.getParent());
                node.getParent().fireEvent(newEvt);
            }
        });
    }
}
