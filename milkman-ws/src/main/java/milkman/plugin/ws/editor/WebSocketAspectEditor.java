package milkman.plugin.ws.editor;

import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.plugin.ws.domain.WebsocketAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.contenttype.PlainContentTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.contenttype.JsonContentType;

import java.util.Arrays;

import static milkman.utils.FunctionalUtils.run;

public class WebSocketAspectEditor implements RequestAspectEditor {

	@Override
	public Tab getRoot(RequestContainer request) {
		WebsocketAspect gqlAspect = request.getAspect(WebsocketAspect.class).get();
		Tab tab = new Tab("Messages");
		
		ContentEditor editor = new ContentEditor();
		editor.setHeaderVisibility(false);
		editor.setEditable(true);
		editor.setContentTypePlugins(Arrays.asList(new JsonContentType(), new PlainContentTypePlugin()));
		editor.setContentType("text/plain");
		editor.setContent(gqlAspect::getMessage, run(gqlAspect::setMessage).andThen(() -> gqlAspect.setDirty(true)));
		
		tab.setContent(editor);
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(WebsocketAspect.class).isPresent();
	}

}
