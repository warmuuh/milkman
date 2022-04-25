package milkman.plugin.sio.editor;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.RequestContainer;
import milkman.plugin.sio.domain.SocketIORequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;

public class SocketIORequestEditor implements RequestTypeEditor, AutoCompletionAware {

	 TextField requestUrl;

	private final GenericBinding<SocketIORequestContainer, String> urlBinding = GenericBinding.of(SocketIORequestContainer::getUrl, SocketIORequestContainer::setUrl);
	private AutoCompleter completer;
	private ExecutionListenerManager executionListenerManager;


	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new SocketIORequestEditorFxml(this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof SocketIORequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");
		
		SocketIORequestContainer restRequest = (SocketIORequestContainer)request;
		
		urlBinding.bindTo(requestUrl.textProperty(), restRequest);
		urlBinding.addListener(s -> request.setDirty(true));
		completer.attachVariableCompletionTo(requestUrl);
	}


	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
		
	}

	public static class SocketIORequestEditorFxml extends HboxExt {
		private final SocketIORequestEditor controller; //avoid gc collection

		public SocketIORequestEditorFxml(SocketIORequestEditor controller) {
			this.controller = controller;
			HBox.setHgrow(this, Priority.ALWAYS);
			controller.requestUrl = add(new JFXTextField(), true);
			controller.requestUrl.setId("requestUrl");
			controller.requestUrl.setPromptText("ws|http(s)://host:port/url");
		}
	}
	
}
