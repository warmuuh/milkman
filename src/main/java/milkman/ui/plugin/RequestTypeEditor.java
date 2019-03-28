package milkman.ui.plugin;

import java.util.EventListener;
import java.util.function.Consumer;

import io.vavr.CheckedConsumer;
import javafx.scene.Node;
import milkman.domain.RequestContainer;

public interface RequestTypeEditor {

	Node getRoot();
	
	void displayRequest(RequestContainer container);
	
}
