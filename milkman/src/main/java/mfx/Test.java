package mfx;

import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableColumn;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.fxml.facade.FxmlBuilder;

import java.util.Comparator;
import java.util.List;

public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Data
    @AllArgsConstructor
    public class TestContainer {
        private String testValue = "b";
        private String testValue2 = "c";

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


        MFXTableView<TestContainer> tableView = new MFXTableView<>();
        ObservableList<TestContainer> people = FXCollections.observableArrayList(List.of(
                new TestContainer("a", "1"),
                new TestContainer("b", "2")
                ));

        MFXTableColumn<TestContainer> column1 = new MFXTableColumn<>("TV1", Comparator.comparing(TestContainer::getTestValue));
        column1.setRowCellFunction(data -> {
            GenericBinding<TestContainer, String> binding = GenericBinding.of(TestContainer::getTestValue, TestContainer::setTestValue, data);
            MFXTableRowEditableCell cell = new MFXTableRowEditableCell(binding);
            return cell;
        });

        MFXTableColumn<TestContainer> column2 = new MFXTableColumn<>("TV2", Comparator.comparing(TestContainer::getTestValue));
        column2.setRowCellFunction(data -> {
            GenericBinding<TestContainer, String> binding = GenericBinding.of(TestContainer::getTestValue2, TestContainer::setTestValue2, data);
            MFXTableRowEditableCell cell = new MFXTableRowEditableCell(binding);
            return cell;
        });

        tableView.setItems(people);
        tableView.getTableColumns().addAll(column1, column2);
        root.add(tableView);


        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
