package milkman.ui.main;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import milkman.ctrl.WorkspaceController;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.utils.Event;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class RequestCollectionComponent {

	
	public final Event<RequestContainer> onRequestSelection = new Event<RequestContainer>();

	
	@FXML Accordion collectionContainer;

	public void display(List<Collection> collections) {
		for (Collection collection : collections) {
			TitledPane pane = createPane(collection);
			collectionContainer.getPanes().add(pane);
		}
	}


	private TitledPane createPane(Collection collection) {
		List<Node> entries = collection.getRequests().stream().map(this::createRequestEntry).collect(Collectors.toList());
		return new TitledPane(collection.getName(), new VBox(entries.toArray(new Node[] {})));
	}


	private Node createRequestEntry(RequestContainer request) {
		Button button = new Button(request.getName());
		button.setOnAction(e -> onRequestSelection.invoke(request));
		return button;
	}
	
}
