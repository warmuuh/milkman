package milkman.ui.components;

import static milkman.utils.fxml.FxmlBuilder.*;

import com.jfoenix.controls.JFXPopup;
import java.util.Map;
import javafx.scene.control.TextArea;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlBuilder.VboxExt;

public class StatusPopup extends JFXPopup {


  public StatusPopup(Map<String, String> entries) {
    setAutoHide(true);
    setHideOnEscape(true);
    setConsumeAutoHidingEvents(false);

    VboxExt root = vbox();
    entries.forEach( (k,v) -> {
//      TextArea textArea = new TextArea(v);
//      textArea.setEditable(false);
      root.add(hbox(label(k + ": "), space(30), label(v)));
    });
    setPopupContent(root);
  }
}
