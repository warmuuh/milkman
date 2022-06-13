package milkman.utils.javafx;

import java.util.List;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;


public class AutoCompleteBox<T> implements EventHandler<KeyEvent> {

  private final ComboBox<T> comboBox;
  private final Function<String, List<T>> lookupFunction;

  public AutoCompleteBox(final ComboBox<T> comboBox, Function<String, List<T>> lookupFunction) {
    this.comboBox = comboBox;
      this.lookupFunction = lookupFunction;
      comboBox.setConverter(new StringConverter<T>() {
        @Override
        public String toString(T object) {
          return object != null ? object.toString() : "";
        }

        @Override
        public T fromString(String string) {
          return comboBox.getItems().stream().filter(itm -> itm.toString().equals(string)).findAny().orElse(null);
        }
      });
      this.doAutoCompleteBox();
  }

  private void doAutoCompleteBox() {
    this.comboBox.setEditable(true);
    this.comboBox.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {//mean onfocus
        this.comboBox.show();
      }
    });

    this.comboBox.getEditor().setOnMouseClicked(event -> {
      if (event.getButton().equals(MouseButton.PRIMARY)) {
        if (event.getClickCount() == 2) {
          return;
        }
      }
      this.comboBox.show();
    });

    this.comboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
      moveCaret(this.comboBox.getEditor().getText().length());
    });

    this.comboBox.setOnKeyPressed(t -> comboBox.hide());

    this.comboBox.setOnKeyReleased(AutoCompleteBox.this);

  }


  @Override
  public void handle(KeyEvent event) {
    if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN
        || event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
        || event.getCode() == KeyCode.HOME
        || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB
    ) {
      return;
    }

    if (event.getCode() == KeyCode.BACK_SPACE) {
      String str = this.comboBox.getEditor().getText();
      if (str != null && str.length() > 0) {
        str = str.substring(0, str.length() - 1);
      }
      if (str != null) {
        this.comboBox.getEditor().setText(str);
        moveCaret(str.length());
      }
      this.comboBox.getSelectionModel().clearSelection();
    }

    if (event.getCode() == KeyCode.ENTER && comboBox.getSelectionModel().getSelectedIndex() > -1) {
      return;
    }

    setItems();
  }

  private void setItems() {
    ObservableList<T> list = FXCollections.observableArrayList();

    this.comboBox.hide();
    String searchString = this.comboBox.getEditor().getText().toLowerCase();
    List<T> foundItems = lookupFunction.apply(searchString);
    list.addAll(foundItems);

    if (!list.isEmpty()) {
      this.comboBox.setItems(list);
      this.comboBox.show();
    }

  }

  private void moveCaret(int textLength) {
    this.comboBox.getEditor().positionCaret(textLength);
  }

}
