package milkman.ui.components;

import javafx.scene.layout.StackPane;
import milkman.ui.main.options.CoreApplicationOptionsProvider;

import static milkman.utils.fxml.facade.FxmlBuilder.spinner;
import static milkman.utils.fxml.facade.FxmlBuilder.staticSpinner;


public class FancySpinner extends StackPane {

	public FancySpinner() {
		getStyleClass().add("spinner");
		if (!CoreApplicationOptionsProvider.options().isDisableAnimations()) {
			getChildren().add(spinner("first-spinner", -40));
			getChildren().add(spinner("second-spinner", -90));
			getChildren().add(spinner("third-spinner", -120));
			getChildren().add(spinner("fourth-spinner", -150));
			getChildren().add(spinner("fifth-spinner", -180));
			getChildren().add(spinner("sixth-spinner", -210));
			getChildren().add(spinner("seventh-spinner", -240));
		} else {
			getChildren().add(staticSpinner("first-spinner"));
		}
	}


}
