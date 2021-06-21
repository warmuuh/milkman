package milkman.utils.fxml.facade;

import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import static milkman.utils.fxml.facade.FxmlBuilder.textArea;

/**
 * a text field that shows a popup if text is too long
 */
public class LongTextField extends JFXTextField {

    private final Popup popup;
    private final TextArea content;

    public LongTextField() {
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(false);

        content = textArea();
        content.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255), null, null)));
        content.setPrefHeight(60);
        content.setWrapText(false);
        popup.getContent().add(new VBox(content));


        setupListener();
    }

    private void showPopup() {
        // run later because otherwise caret position is not yet set
        Platform.runLater(() -> {
            content.setText(getText());
            content.setPrefWidth(getWidth());
            content.positionCaret(getCaretPosition());


            var pos = localToScreen(0, 0);
            popup.show(this, pos.getX(), pos.getY());
            content.textProperty().addListener((o, ol, n) -> setText(n));
        });
    }


    private void setupListener() {
        focusedProperty().addListener( (obs, old, newValue) -> {
            if (!old && newValue){
                var length = Utils.computeTextWidth(getFont(), getText(), 0.0);
                if ( length > getWidth()){
                    showPopup();
                }
            }
        });
    }

}
