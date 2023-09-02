package milkman.ui.components;

import static milkman.utils.fxml.facade.FxmlBuilder.button;
import static milkman.utils.fxml.facade.FxmlBuilder.icon;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import milkman.utils.fxml.facade.FxmlBuilder;

public class TinySpinner extends StackPane {

	private Button cancellation;

	public TinySpinner() {
		this(true);
	}

	public TinySpinner(boolean cancellationEnabled) {
		this.getStyleClass().add("spinner");
		this.getChildren().add(FxmlBuilder.spinner("tiny-spinner", -40));

		if (cancellationEnabled) {
			cancellation = button("tiny-cancellation", icon(FontAwesomeIcon.TIMES, "1.5em"));
			StackPane.setAlignment(cancellation, Pos.CENTER);
			this.getChildren().add(cancellation);
		}
	}

	public final void setOnAction(EventHandler<ActionEvent> value) {
		cancellation.setOnAction(value);
	}

}
