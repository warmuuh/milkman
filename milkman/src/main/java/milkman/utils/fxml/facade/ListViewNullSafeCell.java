package milkman.utils.fxml.facade;

import com.jfoenix.controls.JFXListCell;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class ListViewNullSafeCell<T> extends JFXListCell<T> {
    private final Function<T, Node> nodeFactory;

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(null);
            setGraphic(nodeFactory.apply(item));
        }
    }
}
