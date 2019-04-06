package milkman.ui.plugin.rest;

import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

public class ResponseBodyTabController implements ResponseAspectEditor, ContentTypeAwareEditor {

	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(ResponseAspect aspect, ResponseContainer response) {
		val body = (RestResponseBodyAspect) aspect;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		ContentEditor root = loader.load();
		root.setEditable(false);
		root.setContent(body::getBody, body::setBody);
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		
		response.aspect(RestResponseHeaderAspect.class)
				.map(RestResponseHeaderAspect::contentType)
				.ifPresent(root::setContentType);
		
		return new Tab("Response Body", root);
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseBodyAspect;
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		this.plugins = plugins;
		
	}

}
