package milkman.ui.main.dialogs;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.utils.fxml.FxmlUtil;

public class SaveRequestDialog implements Initializable{

	@FXML ListView<String> collectionList;
	@FXML TextField requestName;
	@FXML TextField collectionName;
	private Stage dialog;

	private RequestContainer request;
	@Getter boolean cancelled = true;
	private List<Collection> collections;
	
	public SaveRequestDialog(RequestContainer request, List<Collection> collections) {
		this.request = request;
		this.collections = collections;
	}

	public void showAndWait() {
		Parent content = FxmlUtil.loadAndInitialize("/dialogs/SaveRequestDialog.fxml", this);
		dialog = FxmlUtil.createDialog("Save Request as", content);
		dialog.showAndWait();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		requestName.setText(request.getName());
		collections.forEach(c -> collectionList.getItems().add(c.getName()));
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
