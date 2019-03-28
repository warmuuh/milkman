package milkman.ui.plugin.rest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.ui.plugin.TabController;

public class RequestHeaderTabController implements TabController {

	@Override
	@SneakyThrows
	public Tab getRoot() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/HeaderComponent.fxml"));
		Parent root = loader.load();
		return new Tab("Headers", root);
	}

}
