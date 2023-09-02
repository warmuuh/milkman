package milkman.ui.plugin.rest;

import static milkman.utils.fxml.facade.FxmlBuilder.icon;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.List;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.ContentEditor;
import milkman.ui.contenttype.PlainContentTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.DebugRequestBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

@Slf4j
public class DebugRequestBodyTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val body = response.getAspect(DebugRequestBodyAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("No debug reqeuest body aspect"));
		ContentEditor root = new ContentEditor();
		root.setEditable(false);
		root.setContentType("text/plain");
		root.setContentTypePlugins(List.of(new PlainContentTypePlugin()));
		response.getAspect(RestResponseHeaderAspect.class)
				.map(RestResponseHeaderAspect::contentType)
				.ifPresent(root::setContentType);

		root.addContent(body.getBody());

		Tab tab = new Tab("Request Body",  root);
		tab.setGraphic(icon(FontAwesomeIcon.BUG, "1em"));
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(DebugRequestBodyAspect.class).isPresent();
	}
}
