package milkman.ui.components;

import com.jfoenix.controls.JFXSpinner;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import static milkman.utils.fxml.facade.FxmlBuilder.button;
import static milkman.utils.fxml.facade.FxmlBuilder.icon;

public class TinySpinner extends StackPane {

	private final Button cancellation;

	public TinySpinner() {
		getStyleClass().add("spinner");
		getChildren().add(spinner("tiny-spinner", -40));
		
		cancellation = button("tiny-cancellation", icon(FontAwesomeIcon.TIMES, "1.5em"));
		StackPane.setAlignment(cancellation, Pos.CENTER);
		getChildren().add(cancellation);
	}

	private JFXSpinner spinner(String cssClass, int startAngle) {
		JFXSpinner spinner = new JFXSpinner();
//		spinner.setBorder();
		spinner.getStyleClass().add(cssClass);
		spinner.setStartingAngle(startAngle);
		return spinner;
	}

	public final void setOnAction(EventHandler<ActionEvent> value) {
		cancellation.setOnAction(value);
	}
	
	
	
}
