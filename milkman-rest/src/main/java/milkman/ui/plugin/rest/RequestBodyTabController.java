package milkman.ui.plugin.rest;

import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.RestBodyAspect;

public class RequestBodyTabController implements RequestAspectEditor, ContentTypeAwareEditor {

	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestAspect aspect) {
		RestBodyAspect body = (RestBodyAspect) aspect;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		ContentEditor root = loader.load();
		root.setEditable(true);
		root.setContent(body::getBody, body::setBody);
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		
		return new Tab("Body", root);
	}

	@Override
	public boolean canHandleAspect(RequestAspect aspect) {
		return aspect instanceof RestBodyAspect;
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		this.plugins = plugins;
		
	}

}
