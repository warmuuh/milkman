package mfx;

import com.jfoenix.controls.JFXToggleNode;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import milkman.utils.fxml.facade.FxmlBuilder;

import static milkman.utils.fxml.facade.FxmlBuilder.icon;

public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        var root = FxmlBuilder.vbox();
        Scene mainScene = new Scene(root);

        mainScene.getStylesheets().clear();
        mainScene.getStylesheets().add("/mfx/light.css");

        var mfx = new MFXRectangleToggleNode();
        mfx.setGraphic(icon(FontAwesomeIcon.GLOBE));
        root.add(mfx);

        JFXToggleNode node = new JFXToggleNode();
        node.setGraphic(icon(FontAwesomeIcon.GLOBE));
        root.add(node);


        ToggleButton javafx = new ToggleButton();
        javafx.setGraphic(icon(FontAwesomeIcon.GLOBE));
        root.add(javafx);

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
