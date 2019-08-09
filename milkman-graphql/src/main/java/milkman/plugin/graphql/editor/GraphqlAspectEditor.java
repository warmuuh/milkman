package milkman.plugin.graphql.editor;

import static milkman.utils.FunctionalUtils.run;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import milkman.domain.RequestContainer;
import milkman.plugin.graphql.domain.GraphqlAspect;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;

public class GraphqlAspectEditor implements RequestAspectEditor {

	@Override
	public Tab getRoot(RequestContainer request) {
		GraphqlAspect gqlAspect = request.getAspect(GraphqlAspect.class).get();
		Tab tab = new Tab("Graphql");
		TextArea textArea = new TextArea();
		GenericBinding<GraphqlAspect, String> binding = GenericBinding.of(
				GraphqlAspect::getQuery, 
				run(GraphqlAspect::setQuery)
					.andThen(() -> gqlAspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
					gqlAspect); 
		textArea.textProperty().bindBidirectional(binding);
		textArea.setUserData(binding); //need to add a strong reference to keep the binding from being GC-collected.
		tab.setContent(textArea);
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GraphqlAspect.class).isPresent();
	}

}
