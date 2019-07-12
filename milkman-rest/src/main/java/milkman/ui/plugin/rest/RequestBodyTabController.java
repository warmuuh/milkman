package milkman.ui.plugin.rest;

import static milkman.utils.FunctionalUtils.run;

import java.util.List;

import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;


public class RequestBodyTabController implements RequestAspectEditor, ContentTypeAwareEditor {

	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		RestBodyAspect body = request.getAspect(RestBodyAspect.class).get();
		ContentEditor root = new ContentEditor();
		root.setEditable(true);
		root.setContent(body::getBody, run(body::setBody).andThen(() -> body.setDirty(true)));
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		
		setContentTypeIfPresent(root, request);
		
		return new Tab("Body", root);
	}

	private void setContentTypeIfPresent(ContentEditor root, RequestContainer request) {
		request.getAspect(RestHeaderAspect.class).flatMap(headers -> 
			headers.getEntries().stream().filter(h -> h.getName().equalsIgnoreCase("Content-Type")).findAny()
		)
		.map(h -> h.getValue())
		.ifPresent(root::setContentType);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(RestBodyAspect.class).isPresent();
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		this.plugins = plugins;
		
	}

}
