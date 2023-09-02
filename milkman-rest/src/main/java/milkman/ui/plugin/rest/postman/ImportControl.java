package milkman.ui.plugin.rest.postman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.SimpleTabPane;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * a control that allows for different ways of importing a file
 * via file-selection, via paste of raw text, via pastebin (planned)
 * 
 * @author peter
 *
 */
@Slf4j
public class ImportControl extends SimpleTabPane {

	
	TextArea rawContent;
	TextField selectedFile;
	
	FileChooser fileChooser;

	public ImportControl() {
		this(true);
	}

	public ImportControl(boolean allowFile) {
		fileChooser = new FileChooser();
		rawContent = new TextArea();
		selectedFile = new TextField();
		
		Button openFileSelectionBtn = new Button("Select...");
		openFileSelectionBtn.setOnAction(e -> {
			File f = fileChooser.showOpenDialog(FxmlUtil.getPrimaryStage());
			if (f != null && f.exists() && f.isFile()) {
				selectedFile.setText(f.getPath());
			}
		});
		
		HBox.setHgrow(selectedFile, Priority.ALWAYS);
		HBox selectionCtrl = new HBox(selectedFile, openFileSelectionBtn);
		selectionCtrl.setPadding(new Insets(10));
		if (allowFile){
			getTabs().add(new Tab("File", selectionCtrl));
		}
		getTabs().add(new Tab("Raw", rawContent));
		VBox.setVgrow(this, Priority.ALWAYS);
	}
	
	
	/**
	 * returns either an input stream from the selected method or null, if invalid/none data was entered
	 */
	public InputStream getInput() {
		if (getSelectionModel().getSelectedItem().getText() == "File") {
			if (StringUtils.isNotBlank(selectedFile.getText())) {
				try {
					return new FileInputStream(new File(selectedFile.getText()));
				} catch (FileNotFoundException e) {
					log.error("File not found", e);
				}
			}
		}
		
		if (getSelectionModel().getSelectedItem().getText() == "Raw") {
			if (StringUtils.isNotBlank(rawContent.getText())) {
				return IOUtils.toInputStream(rawContent.getText());
			}
		}
		
		
		return null;
	}
	
}
