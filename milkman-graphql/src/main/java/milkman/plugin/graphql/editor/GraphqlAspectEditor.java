package milkman.plugin.graphql.editor;

import static milkman.utils.FunctionalUtils.run;

import java.util.Arrays;

import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.plugin.graphql.GraphqlContentType;
import milkman.plugin.graphql.domain.GraphqlAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.RequestAspectEditor;

public class GraphqlAspectEditor implements RequestAspectEditor {

	@Override
	public Tab getRoot(RequestContainer request) {
		GraphqlAspect gqlAspect = request.getAspect(GraphqlAspect.class).get();
		Tab tab = new Tab("Graphql");
		
		ContentEditor editor = new ContentEditor();
		editor.setHeaderVisibility(false);
		editor.setEditable(true);
		editor.setContentTypePlugins(Arrays.asList(new GraphqlContentType()));
		editor.setContentType("application/graphql");
		editor.setContent(gqlAspect::getQuery, run(gqlAspect::setQuery).andThen(() -> gqlAspect.setDirty(true)));
		
		tab.setContent(editor);
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GraphqlAspect.class).isPresent();
	}

}
