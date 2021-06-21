package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import lombok.Getter;
import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.utils.StringUtils;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.FxmlBuilder.*;
import milkman.utils.fxml.facade.ValidatableTextField;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static milkman.utils.fxml.facade.FxmlBuilder.*;

public class SaveRequestDialog {

	 ListView<String> collectionList;
	 ValidatableTextField requestName;
	ValidatableTextField collectionName;
	private Dialog dialog;

	private final RequestContainer request;
	@Getter boolean cancelled = true;
	private final List<Collection> collections;
	
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
		List<String> collectionNames = getCollectionStrings();
		FilteredList<String> filteredList = new FilteredList<String>(FXCollections.observableList(collectionNames));
		collectionName.textProperty().addListener(obs-> {
	        String filter = collectionName.getText(); 
	        if(filter == null || filter.length() == 0) {
	        	Platform.runLater(() -> filteredList.setPredicate(s -> true));
	        }
	        else {
	        	Platform.runLater(() -> filteredList.setPredicate(s -> StringUtils.containsLettersInOrder(s, filter)));
	        }
		});
		collectionList.setItems(filteredList);
		
		
		collectionList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		collectionList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> { 
			if (n != null && n.length() > 0)
				collectionName.setText(n);
		});
	}

    private List<String> getCollectionStrings() {
        return collections.stream().flatMap(c -> {
            var strings = new LinkedList<String>();
            strings.add(c.getName());
            c.getFolders().forEach(f -> addSubfolderStrings(c.getName(), f, strings));
            return strings.stream();
        }).collect(Collectors.toList());
    }

	private void addSubfolderStrings(String prefix, Folder folder, List<String> strings) {
		var curId = prefix + "/" + folder.getName();
		strings.add(curId);
		folder.getFolders().forEach(sf ->  addSubfolderStrings(curId, sf, strings));
	}

    public String getRequestName() {
		return requestName.getText();
	}
	public String getCollectionName() {
		return collectionName.getText().split("/")[0];
	}
	public List<String> getFolderPath() {
        var split = collectionName.getText().split("/");
        if (split.length < 2){
            return Collections.emptyList();
        }

        List<String> folders = new LinkedList<>(Arrays.asList(split));
        folders.remove(0);
	    return folders;
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

			var vbox = new VboxExt();
			vbox.setSpacing(25);
			controller.requestName = vbox.add(vtext("requestName", "Request Name", true));
			controller.requestName.setValidators(requiredValidator());

			controller.collectionName = vbox.add(vtext("collectionName", "Path (collection/folder)", true));
			controller.collectionName.setValidators(requiredValidator());

			var collContainer = vbox.add(new VboxExt());
			collContainer.add(label("Existing Collections:"));
			controller.collectionList = collContainer.add(new ListView<>());
			setBody(vbox);

			setActions(submit(controller::onSave, "Save"),
					cancel(controller::onCancel));

			controller.initialize();
		}
	}

}
