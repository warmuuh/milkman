package mfx;

import com.jfoenix.controls.JFXButton;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import milkman.utils.fxml.facade.FxmlBuilder;

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

        root.add(new MFXButton("mfxButton"));
        root.add(new JFXButton("jfxButton"));
        root.add(new Button("plainButton"));

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
