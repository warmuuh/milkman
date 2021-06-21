package milkman.ui.components;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import milkman.utils.fxml.facade.FxmlBuilder.HboxExt;
import milkman.utils.javafx.JavaFxUtils;

import static milkman.utils.fxml.facade.FxmlBuilder.button;
import static milkman.utils.fxml.facade.FxmlBuilder.text;

public class SearchBox extends HboxExt {

	private SearchRequestHandler handler;
	private Runnable closeHandler;
	private TextField searchField;

	public SearchBox() {
		initLayout();
	}

	private void initLayout() {
		getStyleClass().add("searchBox");
		
		searchField = text();

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
		var searchUp = button();
		searchUp.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ANGLE_UP));
		searchUp.setOnAction(e -> triggerSearch(true));
		var searchDown = button();
		searchDown.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ANGLE_DOWN));
		searchDown.setOnAction(e -> triggerSearch(false));

		add(searchField);
		add(searchUp);
		add(searchDown);
		setMaxWidth(200);
		setMaxHeight(40);
		setVisible(false);

		ChangeListener<Boolean> focusListener = (obs, o, n) -> {
			if (n != null && n == false) {
				var focusedNode = getScene().getFocusOwner();
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
