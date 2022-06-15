package milkman.ui.components;

import static milkman.utils.fxml.FxmlBuilder.button;
import static milkman.utils.fxml.FxmlBuilder.icon;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.StackPane;

public class TinySpinner extends StackPane {

	private JFXButton cancellation;

	public TinySpinner() {
		this(true);
	}

	public TinySpinner(boolean cancellationEnabled) {
		this.getStyleClass().add("spinner");
		this.getChildren().add(spinner("tiny-spinner", -40));

		if (cancellationEnabled) {
			cancellation = button("tiny-cancellation", icon(FontAwesomeIcon.TIMES, "1.5em"));
			StackPane.setAlignment(cancellation, Pos.CENTER);
			this.getChildren().add(cancellation);
		}
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
