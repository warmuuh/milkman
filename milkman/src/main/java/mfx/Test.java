package mfx;

import com.jfoenix.controls.JFXComboBox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyComboBox;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.fxml.facade.FxmlBuilder;

public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Data
    @AllArgsConstructor
    public class TestContainer {
        private String testValue = "b";

        public void setTestValue(String testValue) {
            System.out.println("Setting value: " + testValue);
            this.testValue = testValue;
        }
    }

    private final GenericBinding<TestContainer, String> valueBinding = GenericBinding.of(TestContainer::getTestValue, TestContainer::setTestValue);


    @Override
    public void start(Stage primaryStage) {

        var root = FxmlBuilder.vbox();
        Scene mainScene = new Scene(root);

        mainScene.getStylesheets().clear();
        mainScene.getStylesheets().add("/mfx/light.css");

        root.add(new Label("MFXComboBox"));
        var nmfx = new MFXComboBox<String>();
        nmfx.getItems().add("a");
        nmfx.getItems().add("b");
        nmfx.getItems().add("c");
        root.add(nmfx);

        nmfx.getSelectionModel().selectItem("b");
        valueBinding.bindToUni(nmfx.getSelectionModel().selectedItemProperty(), new TestContainer("b"));


        root.add(new Label("MFXLegacyComboBox"));
        var mfx = new MFXLegacyComboBox<>();
        mfx.getItems().add("a");
        mfx.getItems().add("b");
        mfx.getItems().add("c");
        mfx.setValue("b");
        root.add(mfx);

        root.add(new Label("JFXComboBox"));
        var node = new JFXComboBox<>();
        node.getItems().add("a");
        node.getItems().add("b");
        node.getItems().add("c");
        node.setValue("b");
        root.add(node);


        root.add(new Label("ComboBox"));
        var javafx = new ComboBox<>();
        javafx.getItems().add("a");
        javafx.getItems().add("b");
        javafx.getItems().add("c");
        javafx.setValue("b");
        root.add(javafx);

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
