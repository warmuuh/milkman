package milkman.ui.plugin.rest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.domain.ResponseAspect;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

public class ResponseBodyTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(ResponseAspect aspect) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		Parent root = loader.load();
		return new Tab("Response Body", root);
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseHeaderAspect;
	}

}
