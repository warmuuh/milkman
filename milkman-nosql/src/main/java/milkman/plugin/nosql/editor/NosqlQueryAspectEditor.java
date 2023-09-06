package milkman.plugin.nosql.editor;

import static milkman.utils.FunctionalUtils.run;

import java.util.Collections;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.plugin.nosql.domain.NosqlQueryAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.RequestAspectEditor;

public class NosqlQueryAspectEditor implements RequestAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		val queryAspect = request.getAspect(NosqlQueryAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Nosql Query Aspect missing"));
		
		ContentEditor root = new ContentEditor();
		root.setEditable(true);
		root.setContent(queryAspect::getQuery, run(queryAspect::setQuery).andThen(() -> queryAspect.setDirty(true)));
		root.setContentTypePlugins(Collections.singletonList(new NosqlContentType()));
		root.setContentType("application/nosql");
		return new Tab("Query", root);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(NosqlQueryAspect.class).isPresent();
	}

}
