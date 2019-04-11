package milkman.plugin.scripting;

import java.util.Collections;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.plugin.scripting.conenttype.JavascriptContentType;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.RequestAspectEditor;

public class ScriptingAspectEditor implements RequestAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		val script = request.getAspect(ScriptingAspect.class).get();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		ContentEditor root = loader.load();
		root.setEditable(true);
		root.setContent(script::getPostRequestScript, script::setPostRequestScript);
		root.setContentTypePlugins(Collections.singletonList(new JavascriptContentType()));
		root.setContentType("application/javascript");
		root.setHeaderVisibility(false);
		
		return new Tab("Scripting", root);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(ScriptingAspect.class).isPresent();
	}

}
