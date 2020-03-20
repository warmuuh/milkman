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

    public LongTextField() {
        this.focusedProperty().addListener( (obs, old, newValue) -> {
            if (!old && newValue){
                var length = com.sun.javafx.scene.control.skin.Utils.computeTextWidth(this.getFont(), this.getText(), 0.0);
                if ( length > this.getWidth()){
                    createEditPopup();
                }
            }
        });
    }

    private void createEditPopup() {
        // run later because otherwise caret position is not yet set
        Platform.runLater(() -> {
            var content = new JFXTextArea(this.getText());
            content.positionCaret(this.getCaretPosition());
            content.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255), null, null)));
            content.setPrefHeight(60);
            content.setPrefWidth(this.getWidth());
            content.setWrapText(false);

            var jfxPopup = new Popup();
            jfxPopup.setAutoHide(true);
            jfxPopup.setHideOnEscape(true);
            jfxPopup.setConsumeAutoHidingEvents(false);
            jfxPopup.getContent().add(new VBox(content));
            var pos = localToScreen(0, 0);
            jfxPopup.show(this, pos.getX(), pos.getY());
            content.textProperty().addListener((o, ol, n) -> this.setText(n));
        });
    }
}
