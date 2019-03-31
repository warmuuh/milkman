package milkman.ui.plugin.rest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.ResponseAspect;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

public class ResponseHeaderTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(ResponseAspect aspect) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/HeaderComponent.fxml"));
		Parent root = loader.load();
		return new Tab("Response Headers", root);
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseHeaderAspect;
	}

}
