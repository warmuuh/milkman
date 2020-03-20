package milkman.ui.components;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

/**
 * a text field that shows a popup if text is too long
 */
public class LongTextField extends JFXTextField {

    private final Popup popup;
    private final JFXTextArea content;

    public LongTextField() {
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(false);

        content = new JFXTextArea();
        content.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255), null, null)));
        content.setPrefHeight(60);
        content.setWrapText(false);
        popup.getContent().add(new VBox(content));


        setupListener();
    }

    private void showPopup() {
        // run later because otherwise caret position is not yet set
        Platform.runLater(() -> {
            content.setText(this.getText());
            content.setPrefWidth(this.getWidth());
            content.positionCaret(this.getCaretPosition());


            var pos = localToScreen(0, 0);
            popup.show(this, pos.getX(), pos.getY());
            content.textProperty().addListener((o, ol, n) -> this.setText(n));
        });
    }


    private void setupListener() {
        this.focusedProperty().addListener( (obs, old, newValue) -> {
            if (!old && newValue){
                var length = com.sun.javafx.scene.control.skin.Utils.computeTextWidth(this.getFont(), this.getText(), 0.0);
                if ( length > this.getWidth()){
                    showPopup();
                }
            }
        });
    }

}
