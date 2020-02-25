package milkman.plugin.jdbc.editor;

import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.plugin.jdbc.SqlContentType;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.RequestAspectEditor;

import java.util.Collections;

import static milkman.utils.FunctionalUtils.run;

public class SqlAspectEditor implements RequestAspectEditor {


	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		val sqlAspect = request.getAspect(JdbcSqlAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Jdbc Sql Aspect missing"));
		
		ContentEditor root = new ContentEditor();
		root.setEditable(true);
		root.setContent(sqlAspect::getSql, run(sqlAspect::setSql).andThen(() -> sqlAspect.setDirty(true)));
		root.setContentTypePlugins(Collections.singletonList(new SqlContentType()));
		root.setContentType("application/sql");
		return new Tab("Body", root);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(JdbcSqlAspect.class).isPresent();
	}

}
