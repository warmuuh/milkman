package milkman.ui.main.options;

import java.util.List;
import javafx.scene.layout.VBox;
import lombok.Data;
import milkman.utils.fxml.GenericBinding;

@Data
public class OptionDialogPane extends VBox {
	private final String name;
	private final List<GenericBinding<?, ?>> bindings;
}
