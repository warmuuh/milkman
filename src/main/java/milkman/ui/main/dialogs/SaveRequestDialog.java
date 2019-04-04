package milkman.ui.main.dialogs;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXDialogLayout;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import lombok.Getter;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.utils.fxml.FxmlUtil;

public class SaveRequestDialog implements Initializable{

	@FXML ListView<String> collectionList;
	@FXML TextField requestName;
	@FXML TextField collectionName;
	private Dialog dialog;

	private RequestContainer request;
	@Getter boolean cancelled = true;
	private List<Collection> collections;
	
	public SaveRequestDialog(RequestContainer request, List<Collection> collections) {
		this.request = request;
		this.collections = collections;
	}

	public void showAndWait() {
		JFXDialogLayout content = FxmlUtil.loadAndInitialize("/dialogs/SaveRequestDialog.fxml", this);
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		requestName.setText(request.getName());
		List<String> collectionNames = collections.stream().map(c -> c.getName()).collect(Collectors.toList());
		FilteredList<String> filteredList = new FilteredList<String>(FXCollections.observableList(collectionNames));
		collectionName.textProperty().addListener(obs-> {
	        String filter = collectionName.getText(); 
	        if(filter == null || filter.length() == 0) {
	        	Platform.runLater(() -> filteredList.setPredicate(s -> true));
	        }
	        else {
	        	Platform.runLater(() -> filteredList.setPredicate(s -> s.contains(filter)));
	        }
		});
		collectionList.setItems(filteredList);
		
		
		collectionList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		collectionList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> { 
			if (n != null && n.length() > 0)
				collectionName.setText(n);
		});
	}

	
	public String getRequestName() {
		return requestName.getText();
	}
	public String getCollectionName() {
		return collectionName.getText();
	}
	
	@FXML private void onSave() {
		cancelled = false;
		dialog.close();
	}

	@FXML private void onCancel() {
		cancelled = true;
		dialog.close();
	}

}
