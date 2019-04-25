package milkman.ui.main.dialogs;

import java.util.List;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTabPane;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.OptionPageProvider;
import milkman.utils.fxml.FxmlUtil;

public class OptionsDialog {
	@FXML TabPane tabs;
	private Dialog dialog;

	
	public void showAndWait(List<OptionPageProvider> optionPageProviders) {
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/OptionsDialog.fxml", this);
		content.setPrefWidth(600);
		optionPageProviders.sort((a,b) -> a.getOrder() - b.getOrder());
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
	
}
