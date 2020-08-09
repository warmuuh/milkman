package milkman.ui.components;

import com.jfoenix.controls.JFXSpinner;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import milkman.ui.main.options.CoreApplicationOptionsProvider;

public class FancySpinner extends StackPane {

	public FancySpinner() {
		this.getStyleClass().add("spinner");
		if (!CoreApplicationOptionsProvider.options().isDisableAnimations()) {
			this.getChildren().add(spinner("first-spinner", -40));
			this.getChildren().add(spinner("second-spinner", -90));
			this.getChildren().add(spinner("third-spinner", -120));
			this.getChildren().add(spinner("fourth-spinner", -150));
			this.getChildren().add(spinner("fifth-spinner", -180));
			this.getChildren().add(spinner("sixth-spinner", -210));
			this.getChildren().add(spinner("seventh-spinner", -240));
		} else {
			this.getChildren().add(staticSpinner("first-spinner"));
		}
	}

	private JFXSpinner spinner(String cssClass, int startAngle) {
		JFXSpinner spinner = new JFXSpinner();
		spinner.getStyleClass().add(cssClass);
		spinner.setStartingAngle(startAngle);
		return spinner;
	}


	private ProgressIndicator staticSpinner(String cssClass) {
		ProgressIndicator spinner = new ProgressIndicator();
		spinner.getStyleClass().add(cssClass);
		return spinner;
	}

}
