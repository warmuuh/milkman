package milkman.plugin.nosql.editor;

import static milkman.utils.FunctionalUtils.run;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.Collections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import lombok.SneakyThrows;
import lombok.val;
import milkman.PlatformUtil;
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
		
		ContentEditor editor = new ContentEditor();
		editor.setEditable(true);
		editor.setContent(queryAspect::getQuery, run(queryAspect::setQuery).andThen(() -> queryAspect.setDirty(true)));
		editor.setContentTypePlugins(Collections.singletonList(new NosqlContentType()));
		editor.setContentType("application/nosql");

		JFXButton helpBtn = new JFXButton();
		helpBtn.setOnAction(e -> PlatformUtil.tryOpenBrowser("https://github.com/eclipse/jnosql/blob/main/COMMUNICATION.adoc#querying-by-text-with-the-communication-api"));
		helpBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.QUESTION_CIRCLE, "1.5em"));

		editor.addExtraHeaderElement(helpBtn, false);


		return new Tab("Query", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(NosqlQueryAspect.class).isPresent();
	}

}
