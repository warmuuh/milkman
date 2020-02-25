package milkman.ui.main.options;

import javafx.scene.layout.VBox;
import lombok.Data;
import milkman.utils.fxml.GenericBinding;

import java.util.List;

@Data
public class OptionDialogPane extends VBox {
	private final String name;
	private final List<GenericBinding<?, ?>> bindings;
}
