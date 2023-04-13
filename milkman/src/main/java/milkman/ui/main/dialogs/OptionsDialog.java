package milkman.ui.main.dialogs;

import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.label;

import com.jfoenix.controls.JFXDialogLayout;
import java.util.List;
import javafx.geometry.Side;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.utils.fxml.FxmlUtil;

public class OptionsDialog {
	 TabPane tabs;
	private Dialog dialog;

	
	public void showAndWait(List<OptionPageProvider> optionPageProviders) {
		JFXDialogLayout content = new OptionsDialogFxml(this);
		content.setPrefWidth(600);
		for(OptionPageProvider<?> p : optionPageProviders) {
			OptionDialogPane pane = p.getOptionsDialog(new OptionDialogBuilder());
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setContent(pane);
			scrollPane.setFitToHeight(true);
			scrollPane.setFitToWidth(true);

			Tab tab = new Tab("", scrollPane);
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
	
	
	 public void onClose() {
		dialog.close();
	}


	public class OptionsDialogFxml extends JFXDialogLayout {
		public OptionsDialogFxml(OptionsDialog controller){
			setHeading(label("Options"));

			var tabs = controller.tabs = new TabPane();
			tabs.setId("tabs");
//			tabs.setRotateGraphic(true);
			tabs.setSide(Side.LEFT);
			tabs.getStyleClass().add("options-tabs");
			tabs.setPrefHeight(400.0);
			tabs.setPrefWidth(400.0);
			//javafx setters seem to be broken, have to use property for these configs
//			tabs.setTabMinWidth(40);
//			tabs.setTabMaxWidth(40);
			tabs.tabMinWidthProperty().setValue(40);
			tabs.tabMaxWidthProperty().setValue(40);
//			tabs.setMinHeight(150);
//			tabs.setTabMaxHeight(150);
			tabs.tabMinHeightProperty().setValue(150);
			tabs.tabMaxHeightProperty().setValue(150);
			tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
			setBody(tabs);

			setActions(cancel(controller::onClose, "Close"));
		}
	}
}
