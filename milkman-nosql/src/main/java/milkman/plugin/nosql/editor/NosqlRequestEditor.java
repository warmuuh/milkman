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

	
	 TextField url;
	
	private GenericBinding<NosqlRequestContainer, String> urlBinding = GenericBinding.of(NosqlRequestContainer::getUrl, NosqlRequestContainer::setUrl);

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
		urlBinding.bindTo(url.textProperty(), restRequest);
		urlBinding.addListener(s -> request.setDirty(true));
		completer.attachVariableCompletionTo(url);
		
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
			controller.url = add(new JFXTextField(), true);
			controller.url.setId("url");
			controller.url.setPromptText("host:port");
		}
	}
	

}
