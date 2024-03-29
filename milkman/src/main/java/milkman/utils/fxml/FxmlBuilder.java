package milkman.utils.fxml;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleNode;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.function.Consumer;

public class FxmlBuilder {

	public static Label label(Node graphic) {
		Label lbl = new Label();
		lbl.setGraphic(graphic);
		return lbl;
	}
	public static Label label(String text) {
		return new Label(text);
	}

	public static JFXButton button(String text, Runnable onAction) {
		JFXButton jfxButton = new JFXButton(text);
		jfxButton.setOnAction(e -> onAction.run());
		return jfxButton;
	}
	
	public static JFXButton button(String id, String text, Runnable onAction) {
		JFXButton jfxButton = button(text, onAction);
		jfxButton.setId(id);
		return jfxButton;
	}
	
	public static JFXButton button(Node graphic, Runnable onAction) {
		JFXButton jfxButton = new JFXButton();
		jfxButton.setGraphic(graphic);
		jfxButton.setOnAction(e -> onAction.run());
		return jfxButton;
	}


	public static JFXToggleNode toggle(Node graphic, Consumer<Boolean> onAction) {
		JFXToggleNode jfxButton = new JFXToggleNode();
		jfxButton.setGraphic(graphic);
		jfxButton.setOnAction(e -> onAction.accept(jfxButton.isSelected()));
		return jfxButton;
	}

	public static JFXButton button(String id, Node graphic, Runnable onAction) {
		JFXButton button = button(graphic, onAction);
		button.setId(id);
		return button;
	}
	public static JFXButton button(String id, Node graphic) {
		JFXButton jfxButton = new JFXButton();
		jfxButton.setGraphic(graphic);
		jfxButton.setId(id);
		return jfxButton;
	}


	public static RequiredFieldValidator requiredValidator(){
		 RequiredFieldValidator validator = new RequiredFieldValidator("Input Required!");
		validator.setIcon(icon(FontAwesomeIcon.WARNING));
		validator.getIcon().setStyle("-fx-font-family: FontAwesome;");
		return validator;
	}
	public static JFXTextField text(String id, String prompt) {
		return text(id, prompt, false);
	}
	
	public static <O> Node formEntry(String name, GenericBinding<O, String> binding, O object){
		var textField = text(name + "-input", name, true);
		binding.bindTo(textField.textProperty(), object);
		return textField;
	}

	public static JFXTextField text(String id, String prompt, boolean lblFloat) {
		JFXTextField txt = new JFXTextField();
		txt.setId(id);
		txt.setPromptText(prompt);
		txt.setLabelFloat(lblFloat);
		return txt;
	}

	public static JFXButton submit(Runnable action) {
		return submit(action, "Apply");
	}

	public static JFXButton submit(Runnable action, String text){
		JFXButton apply = new JFXButton(text);
		apply.setDefaultButton(true);
		apply.setOnAction(e -> action.run());
		return apply;
	}

	public static JFXButton cancel(Runnable action) {
		return cancel(action, "Cancel");
	}

	public static JFXButton cancel(Runnable action, String text){
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

	public static Region vspace(double space) {
		var spacer = new Region();
		spacer.setMinHeight(space);
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


	public static JFXComboBox comboBox(String id) {
		JFXComboBox cb = new JFXComboBox<>();
		cb.setId(id);
		cb.setEditable(false);
		return cb;
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
