package milkman.ui.components;

import com.jfoenix.controls.JFXTextField;
import java.util.Map;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.PopOver;

public class StatusPopup extends PopOver {

  private final GridPane grid;

  public StatusPopup(Map<String, String> entries) {
    setAutoHide(true);
    setHideOnEscape(true);
    setConsumeAutoHidingEvents(false);

    setArrowLocation(ArrowLocation.RIGHT_CENTER);

    // Build a table-like layout: keys left, values right-aligned
    grid = new GridPane();
    grid.setPadding(new Insets(5));
    ColumnConstraints leftCol = new ColumnConstraints();
    ColumnConstraints rightCol = new ColumnConstraints();
    leftCol.setHgrow(Priority.ALWAYS); // allow keys to take remaining space
    rightCol.setHalignment(HPos.RIGHT); // right-align values
    grid.getColumnConstraints().addAll(leftCol, rightCol);
    grid.setHgap(8);
    grid.setVgap(4);

    int row = 0;
    for (Map.Entry<String, String> e : entries.entrySet()) {
      var key = new Label(e.getKey() + ":");
      var val = new TextField(e.getValue());
      grid.add(key, 0, row);
      grid.add(val, 1, row);
      row++;
    }

//    setContentNode(grid);
    setContentNode(grid);
  }

}
