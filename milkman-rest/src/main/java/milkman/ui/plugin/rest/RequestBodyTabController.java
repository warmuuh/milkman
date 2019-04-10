package milkman.ui.plugin.rest;

import static milkman.utils.FunctionalUtils.run;

import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
public class RequestBodyTabController implements RequestAspectEditor, ContentTypeAwareEditor {

	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		RestBodyAspect body = request.getAspect(RestBodyAspect.class).get();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		ContentEditor root = loader.load();
		root.setEditable(true);
		root.setContent(body::getBody, run(body::setBody).andThen(() -> body.setDirty(true)));
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		
		return new Tab("Body", root);
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
