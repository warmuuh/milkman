package milkman.plugin.nosql.editor;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.nosql.domain.NosqlRequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;

public class NosqlRequestEditor implements RequestTypeEditor, AutoCompletionAware {

	
	 TextField database;
	
	private GenericBinding<NosqlRequestContainer, String> databaseBinding = GenericBinding.of(NosqlRequestContainer::getDatabase, NosqlRequestContainer::setDatabase);

	private AutoCompleter completer;
	
	
	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new NosqlRequestEditorFxml(this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof NosqlRequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");

		NosqlRequestContainer restRequest = (NosqlRequestContainer)request;
		databaseBinding.bindTo(database.textProperty(), restRequest);
		databaseBinding.addListener(s -> request.setDirty(true));
		completer.attachVariableCompletionTo(database);
		
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}

	public static class NosqlRequestEditorFxml extends HboxExt {
		private NosqlRequestEditor controller; //avoid gc collection

		public NosqlRequestEditorFxml(NosqlRequestEditor controller) {
			this.controller = controller;
			HBox.setHgrow(this, Priority.ALWAYS);
			controller.database = add(new JFXTextField(), true);
			controller.database.setId("database");
			controller.database.setPromptText("database");
		}
	}
	

}
