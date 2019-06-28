package milkman.ui.components;

import com.jfoenix.controls.JFXSpinner;

import javafx.scene.layout.StackPane;

public class FancySpinner extends StackPane {

	public FancySpinner() {
		this.getStyleClass().add("spinner");
		this.getChildren().add(spinner("first-spinner", -40));
		this.getChildren().add(spinner("second-spinner", -90));
		this.getChildren().add(spinner("third-spinner", -120));
		this.getChildren().add(spinner("fourth-spinner", -150));
		this.getChildren().add(spinner("fifth-spinner", -180));
		this.getChildren().add(spinner("sixth-spinner", -210));
		this.getChildren().add(spinner("seventh-spinner", -240));
	}

	private JFXSpinner spinner(String cssClass, int startAngle) {
		JFXSpinner spinner = new JFXSpinner();
		spinner.getStyleClass().add(cssClass);
		spinner.setStartingAngle(startAngle);
		return spinner;
	}
	
}
