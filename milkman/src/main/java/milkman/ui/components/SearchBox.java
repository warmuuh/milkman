package milkman.ui.components;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.javafx.JavaFxUtils;

public class SearchBox extends HboxExt {

	private SearchRequestHandler handler;
	private Runnable closeHandler;
	private TextField searchField;

	public SearchBox() {
		initLayout();
	}

	private void initLayout() {
		this.getStyleClass().add("searchBox");
		
		searchField = new JFXTextField();

		searchField.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				if (closeHandler != null) {
					closeHandler.run();
				}
			}
		});

		searchField.setPromptText("Search");
		searchField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				triggerSearch(e.isShiftDown());
			}
		});
		var searchUp = new JFXButton();
		searchUp.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ANGLE_UP));
		searchUp.setOnAction(e -> triggerSearch(true));
		var searchDown = new JFXButton();
		searchDown.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ANGLE_DOWN));
		searchDown.setOnAction(e -> triggerSearch(false));
		
		this.add(searchField);
		this.add(searchUp);
		this.add(searchDown);
		this.setMaxWidth(200);
		this.setMaxHeight(40);
		this.setVisible(false);

		ChangeListener<Boolean> focusListener = (obs, o, n) -> {
			if (n != null && n == false) {
				var focusedNode = this.getScene().getFocusOwner();
				if (!JavaFxUtils.isParent(this, focusedNode)) {
					if (closeHandler != null) {
						closeHandler.run();
					}
				}
			}
		};
		searchField.focusedProperty().addListener(focusListener);
		searchUp.focusedProperty().addListener(focusListener);
		searchDown.focusedProperty().addListener(focusListener);

	}

	protected void triggerSearch(boolean reverse) {
		if (searchField.getText().length() > 0) {
			if (handler != null) {
				handler.onSearch(searchField.getText(), !reverse);
			}
		}
	}

	@Override
	public void requestFocus() {
		searchField.requestFocus();
	}
	
	public void onSearch(SearchRequestHandler handler) {
		this.handler = handler;
	}
	public void onCloseRequest(Runnable closeHandler) {
		this.closeHandler = closeHandler;
	}

	public interface SearchRequestHandler {
		void onSearch(String text, boolean forward);
	}

}
