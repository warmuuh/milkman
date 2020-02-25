package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import lombok.Getter;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;

import java.util.List;
import java.util.stream.Collectors;

import static milkman.utils.fxml.FxmlBuilder.*;

public class SaveRequestDialog {

	 ListView<String> collectionList;
	 JFXTextField requestName;
	 JFXTextField collectionName;
	private Dialog dialog;

	private RequestContainer request;
	@Getter boolean cancelled = true;
	private List<Collection> collections;
	
	public SaveRequestDialog(RequestContainer request, List<Collection> collections) {
		this.request = request;
		this.collections = collections;
	}

	public void showAndWait() {
		JFXDialogLayout content = new SaveRequestDialogFxml(this);
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	public void initialize() {
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
	
	 private void onSave() {
		if (requestName.validate() && collectionName.validate()) {
			cancelled = false;
			dialog.close();
		}
	}

	 private void onCancel() {
		cancelled = true;
		dialog.close();
	}


	public static class SaveRequestDialogFxml extends JFXDialogLayout {
		public SaveRequestDialogFxml(SaveRequestDialog controller){
			setHeading(label("Save Request"));

			var vbox = new FxmlBuilder.VboxExt();
			vbox.add(label("Request Name"));
			controller.requestName = vbox.add(new JFXTextField());
			controller.requestName.setValidators(requiredValidator());

			vbox.add(label("Collection:"));
			controller.collectionName = vbox.add(new JFXTextField());
			controller.collectionName.setValidators(requiredValidator());

			vbox.add(label("Existing Collections:"));
			controller.collectionList = vbox.add(new ListView<>());
			setBody(vbox);

			setActions(submit(controller::onSave, "Save"),
					cancel(controller::onCancel));

			controller.initialize();
		}
	}

}
