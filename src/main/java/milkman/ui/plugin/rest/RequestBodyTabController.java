package milkman.ui.plugin.rest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.RestBodyAspect;

public class RequestBodyTabController implements RequestAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestAspect aspect) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		Parent root = loader.load();
		return new Tab("Body", root);
	}

	@Override
	public boolean canHandleAspect(RequestAspect aspect) {
		return aspect instanceof RestBodyAspect;
	}

}
