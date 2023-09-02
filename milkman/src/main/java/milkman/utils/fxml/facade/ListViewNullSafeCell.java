package milkman.utils.fxml.facade;

import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.cell.MFXListCell;
import java.util.function.Function;
import javafx.scene.Node;

public class ListViewNullSafeCell<T> extends MFXListCell<T> {
    private final Function<T, Node> nodeFactory;

    public ListViewNullSafeCell(MFXListView<T> listView, T data, Function<T, Node> nodeFactory) {
        super(listView, data);
        this.nodeFactory = nodeFactory;
    }

    @Override
    public void updateItem(T item) {
        setData(item);
        if (item == null) {
            render(null);
        } else {
            getChildren().setAll(rippleGenerator, nodeFactory.apply(item));
        }
    }
}
