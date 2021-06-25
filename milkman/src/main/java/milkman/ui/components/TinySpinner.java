package milkman.ui.components;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import milkman.utils.fxml.facade.FxmlBuilder;

import static milkman.utils.fxml.facade.FxmlBuilder.button;
import static milkman.utils.fxml.facade.FxmlBuilder.icon;

public class TinySpinner extends StackPane {

	private final Button cancellation;

	public TinySpinner() {
		getStyleClass().add("spinner");
		getChildren().add(FxmlBuilder.spinner("tiny-spinner", -40));
		
		cancellation = button("tiny-cancellation", icon(FontAwesomeIcon.TIMES, "1.5em"));
		StackPane.setAlignment(cancellation, Pos.CENTER);
		getChildren().add(cancellation);
	}

	public final void setOnAction(EventHandler<ActionEvent> value) {
		cancellation.setOnAction(value);
	}
	
}
