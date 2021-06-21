package milkman.utils.fxml.facade;

import com.jfoenix.controls.JFXPopup;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class SimplePopup extends JFXPopup {
    public enum PopupHPosition {
        RIGHT, LEFT
    }

    public enum PopupVPosition {
        TOP, BOTTOM
    }

    public SimplePopup(Region content) {
        super(content);
    }

    public void show(Node node, PopupVPosition vAlign, PopupHPosition hAlign) {
        super.show(node,
                JFXPopup.PopupVPosition.valueOf(vAlign.name()),
                JFXPopup.PopupHPosition.valueOf(hAlign.name()));
    }
}
