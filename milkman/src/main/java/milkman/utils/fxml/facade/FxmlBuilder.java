package milkman.utils.fxml.facade;

import com.jfoenix.controls.*;
import com.jfoenix.validation.IntegerValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import milkman.utils.fxml.GenericBinding;

import java.util.function.Consumer;
import java.util.function.Function;

public class FxmlBuilder {

    public static Label label(Node graphic) {
        Label lbl = new Label();
        lbl.setGraphic(graphic);
        return lbl;
    }

    public static Label label(String text) {
        return new Label(text);
    }

    public static Button button() {
        return button("");
    }

    public static Button button(String title) {
        var button = new MFXButton(title);
        button.getStyleClass().add("mm-button");
        return button;
    }

    public static Button button(String text, Runnable onAction) {
        var b = button(text);
        b.setOnAction(e -> onAction.run());
        return b;
    }

    public static Button button(String id, String text, Runnable onAction) {
        Button button = button(text, onAction);
        button.setId(id);
        return button;
    }

    public static Button button(Node graphic, Runnable onAction) {
        Button button = button();
        button.setGraphic(graphic);
        button.setOnAction(e -> onAction.run());
        return button;
    }

    public static Button button(String id, Node graphic, Runnable onAction) {
        Button button = button(graphic, onAction);
        button.setId(id);
        return button;
    }

    public static Button button(String id, Node graphic) {
        MFXButton b = new MFXButton();
        b.setGraphic(graphic);
        b.setId(id);
        return b;
    }

    public static CheckBox checkbox(String text) {
        return new MFXCheckbox(text);
    }

    public static ToggleButton toggle(String text) {
        MFXToggleButton toggleButton = new MFXToggleButton();
        toggleButton.setPrefHeight(30);
        toggleButton.setText(text);
        return toggleButton;
    }

    public static ToggleButton toggle(Node graphic) {
        MFXRectangleToggleNode toggleNode = new MFXRectangleToggleNode();
        toggleNode.setPrefHeight(30);
        toggleNode.setPrefWidth(40);
        toggleNode.setGraphic(graphic);
        return toggleNode;
    }

    public static ToggleButton toggle(Node graphic, Consumer<Boolean> onAction) {
        JFXToggleNode jfxButton = new JFXToggleNode();
        jfxButton.setGraphic(graphic);
        jfxButton.setOnAction(e -> onAction.accept(jfxButton.isSelected()));
        return jfxButton;
    }

    public static <T> ComboBox<T> combobox() {
        return new JFXComboBox<T>();
    }

    public static <T> ListView<T> list(double verticalGap, Function<T, Node> cellGraphicFactory) {
        var listView = new JFXListView<T>();
        listView.setCellFactory(l -> new ListViewNullSafeCell<>(cellGraphicFactory));
        listView.setVerticalGap(verticalGap);
        return listView;
    }

    public static RequiredFieldValidator requiredValidator() {
        RequiredFieldValidator validator = new RequiredFieldValidator("Input Required!");
        validator.setIcon(icon(FontAwesomeIcon.WARNING));
        validator.getIcon().setStyle("-fx-font-family: FontAwesome;");
        return validator;
    }

    public static IntegerValidator integerValidator() {
        return new IntegerValidator();
    }

	public static IntegerValidator integerValidator(String errorMessage) {
		return new IntegerValidator("Not an integer");
	}

    public static TextField text(String id, String prompt) {
        return text(id, prompt, false);
    }

    public static <O> Node formEntry(String name, GenericBinding<O, String> binding, O object) {
        var textField = text(name + "-input", name, true);
        binding.bindTo(textField.textProperty(), object);
        return textField;
    }

    public static TextField text(String text) {
        return new JFXTextField(text);
    }

    public static TextField text() {
        return new JFXTextField();
    }

	public static TextField password() {
		return new JFXPasswordField();
	}

    public static ValidatablePaswordField vpassword() {
        return new ValidatablePaswordField();
    }

    public static ValidatableTextField vtext() {
        ValidatableTextField txt = new ValidatableTextField();
        return txt;
    }

    public static ValidatableTextField vtext(String id, String prompt, boolean lblFloat) {
        ValidatableTextField txt = new ValidatableTextField();
        txt.setId(id);
        txt.setPromptText(prompt);
        txt.setLabelFloat(lblFloat);
        return txt;
    }

    public static TextField text(String id, String prompt, boolean lblFloat) {
        JFXTextField txt = new JFXTextField();
        txt.setId(id);
        txt.setPromptText(prompt);
        txt.setLabelFloat(lblFloat);
        return txt;
    }

    public static TextArea textArea() {
        return new JFXTextArea();
    }


    public static Button submit(Runnable action) {
        return submit(action, "Apply");
    }

    public static Button submit(Runnable action, String text) {
        JFXButton apply = new JFXButton(text);
        apply.setDefaultButton(true);
        apply.setOnAction(e -> action.run());
        return apply;
    }

    public static Button cancel(Runnable action) {
        return cancel(action, "Cancel");
    }

    public static Button cancel(Runnable action, String text) {
        JFXButton cancel = new JFXButton(text);
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> action.run());
        return cancel;
    }


    public static Node icon(FontAwesomeIcon icon) {
        return icon(icon, "1.5em");
    }

    public static Node icon(FontAwesomeIcon icon, String size) {
        FontAwesomeIconView view = new FontAwesomeIconView(icon, size);
        view.setStyleClass("icon");
        return view;
    }

    public static Region space(double space) {
        var spacer = new Region();
        spacer.setMinWidth(space);
        return spacer;
    }

    public static Region space() {
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static ChoiceBox choiceBox(String id) {
        ChoiceBox cb = new ChoiceBox();
        cb.setId(id);
        return cb;
    }

	public static ProgressIndicator spinner() {
		JFXSpinner spinner = new JFXSpinner();
		return spinner;
	}

    public static ProgressIndicator spinner(String cssClass, int startAngle) {
        JFXSpinner spinner = new JFXSpinner();
        spinner.getStyleClass().add(cssClass);
        spinner.setStartingAngle(startAngle);
        return spinner;
    }


    public static ProgressIndicator staticSpinner(String cssClass) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.getStyleClass().add(cssClass);
        return spinner;
    }

    public static HboxExt hbox(Node... elements) {
        HboxExt hBox = new HboxExt();
        hBox.getChildren().addAll(elements);
        return hBox;
    }

    public static HboxExt hbox(String id) {
        HboxExt hBox = new HboxExt();
        hBox.setId(id);
        return hBox;
    }

    public static VboxExt vbox(String id) {
        VboxExt vbox = new VboxExt();
        vbox.setId(id);
        return vbox;
    }

    public static VboxExt vbox(Node... elements) {
        VboxExt vBox = new VboxExt();
        vBox.getChildren().addAll(elements);
        return vBox;
    }

    public static <T extends Node> T anchorNode(T node, Double top, Double right, Double bottom, Double left) {
        AnchorPane.setTopAnchor(node, top);
        AnchorPane.setRightAnchor(node, right);
        AnchorPane.setBottomAnchor(node, bottom);
        AnchorPane.setLeftAnchor(node, left);
        return node;
    }

    public static <T> TreeView<T> treeView() {
        return new JFXTreeView<T>();
    }

    public static TabPane tabPane() {
        JFXTabPane jfxTabPane = new JFXTabPane();
        jfxTabPane.setDisableAnimation(true);
        return jfxTabPane;
    }


    public static Tooltip installTooltipAt(Node node) {
        var tt = new JFXTooltip();
        JFXTooltip.setVisibleDuration(Duration.millis(10000));
        JFXTooltip.install(node, tt, Pos.BOTTOM_CENTER);

        return tt;
    }



    public static class HboxExt extends HBox {

        public HboxExt() {
        }

        public HboxExt(double spacing, Node... children) {
            super(spacing, children);
        }

        public HboxExt(double spacing) {
            super(spacing);
        }

        public HboxExt(Node... children) {
            super(children);
        }


        public <T extends Node> T add(T node) {
            getChildren().add(node);
            return node;
        }

        public <T extends Node> T add(T node, boolean grow) {
            getChildren().add(node);
            if (grow)
                HBox.setHgrow(node, Priority.ALWAYS);
            return node;
        }
    }

    public static class VboxExt extends VBox {

        public VboxExt() {
            // TODO Auto-generated constructor stub
        }

        public VboxExt(double spacing, Node... children) {
            super(spacing, children);
            // TODO Auto-generated constructor stub
        }

        public VboxExt(double spacing) {
            super(spacing);
            // TODO Auto-generated constructor stub
        }

        public VboxExt(Node... children) {
            super(children);
            // TODO Auto-generated constructor stub
        }

        public <T extends Node> T add(T node) {
            getChildren().add(node);
            return node;
        }

        public <T extends Node> T add(T node, boolean grow) {
            getChildren().add(node);
            if (grow)
                VBox.setVgrow(node, Priority.ALWAYS);
            return node;
        }

    }
}
