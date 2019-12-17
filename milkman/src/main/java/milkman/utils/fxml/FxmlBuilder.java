package milkman.utils.fxml;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FxmlBuilder {

	public static Label label(Node graphic) {
		Label lbl = new Label();
		lbl.setGraphic(graphic);
		return lbl;
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
	
	
	public static TextField text(String id, String prompt) {
		TextField txt = new JFXTextField();
		txt.setId(id);
		txt.setPromptText(prompt);
		return txt;
	}
	
	public static Node icon(FontAwesomeIcon icon) {
		return icon(icon, "1.5em");
	}
	
	public static Node icon(FontAwesomeIcon icon, String size) {
		FontAwesomeIconView view = new FontAwesomeIconView(icon, size);
		view.setStyleClass("icon");
		return view;
	}
	
	
	public static ChoiceBox choiceBox(String id) {
		ChoiceBox cb = new ChoiceBox();
		cb.setId(id);
		return cb;
	}
	
	public static HboxExt hbox(String id) {
		HboxExt hBox = new HboxExt();
		hBox.setId(id);
		return hBox;
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
			super();
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
			this.getChildren().add(node);
			return node;
		}
		public <T extends Node> T add(T node, boolean grow) {
			this.getChildren().add(node);
			if (grow)
				HBox.setHgrow(node, Priority.ALWAYS);
			return node;
		}
	}
	
	public static class VboxExt extends VBox {

		public VboxExt() {
			super();
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
			this.getChildren().add(node);
			return node;
		}
		public <T extends Node> T add(T node, boolean grow) {
			this.getChildren().add(node);
			if (grow)
				VBox.setVgrow(node, Priority.ALWAYS);
			return node;
		}
		
	}
}
