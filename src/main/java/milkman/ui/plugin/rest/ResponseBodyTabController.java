package milkman.ui.plugin.rest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.ResponseAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;

public class ResponseBodyTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(ResponseAspect aspect) {
		val body = (RestResponseBodyAspect) aspect;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		Parent root = loader.load();
		ContentEditor controller = loader.getController();
		
		controller.setContent(body.getBody());
		
		return new Tab("Response Body", root);
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseBodyAspect;
	}

}
