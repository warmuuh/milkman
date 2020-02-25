package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.utils.fxml.FxmlUtil;

import java.util.List;

import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.label;

public class OptionsDialog {
	@FXML TabPane tabs;
	private Dialog dialog;

	
	public void showAndWait(List<OptionPageProvider> optionPageProviders) {
		JFXDialogLayout content = new OptionsDialogFxml(this);
		content.setPrefWidth(600);
		for(OptionPageProvider<?> p : optionPageProviders) {
			OptionDialogPane pane = p.getOptionsDialog(new OptionDialogBuilder());
			Tab tab = new Tab("", pane);
			Label label = new Label(pane.getName());
			label.setRotate(90);
			label.setMinWidth(100);
			label.setMaxWidth(100);
			label.setMinHeight(40);
			label.setMaxHeight(40);
			tab.setGraphic(label);
			tabs.getTabs().add(tab);
			
		}

		dialog = FxmlUtil.createDialog(content);
		
		dialog.showAndWait();
	}
	
	
	@FXML public void onClose() {
		dialog.close();
	}


	public class OptionsDialogFxml extends JFXDialogLayout {
		public OptionsDialogFxml(OptionsDialog controller){
			setHeading(label("Options"));

			var tabs = controller.tabs = new TabPane();
			tabs.setPrefHeight(400.0);
			tabs.setPrefWidth(400.0);
			tabs.setTabMinWidth(40);
			tabs.setTabMaxWidth(40);
			tabs.setMinHeight(100);
			tabs.setTabMaxHeight(100);
			tabs.getStyleClass().add("options-tabs");
			tabs.setSide(Side.LEFT);
			tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
			tabs.setRotateGraphic(false);
			setBody(tabs);

			setActions(cancel(controller::onClose, "Close"));
		}
	}
}
