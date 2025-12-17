package milkman.plugin.mcp.editor;

import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.mcp.domain.McpRequestContainer;
import milkman.plugin.mcp.domain.McpTransportType;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.GenericBinding;

public class McpRequestEditor implements RequestTypeEditor, AutoCompletionAware {


	ChoiceBox<McpTransportType> type;
	TextField url;


	private GenericBinding<McpRequestContainer, String> urlBinding = GenericBinding.of(McpRequestContainer::getUrl, McpRequestContainer::setUrl);
	private GenericBinding<McpRequestContainer, McpTransportType> typeBinding = GenericBinding.of(McpRequestContainer::getTransport, McpRequestContainer::setTransport);

	private AutoCompleter completer;
	
	
	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new McpRequestEditorFxml(this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof McpRequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");

		McpRequestContainer restRequest = (McpRequestContainer)request;
		urlBinding.bindTo(url.textProperty(), restRequest);
		urlBinding.addListener(s -> request.setDirty(true));
		
		typeBinding.bindTo(type.valueProperty(), restRequest);
		typeBinding.addListener(s -> request.setDirty(true));

		completer.attachVariableCompletionTo(url);
		
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}

	public static class McpRequestEditorFxml extends HboxExt {
		private McpRequestEditor controller; //avoid gc collection

		public McpRequestEditorFxml(McpRequestEditor controller) {
			this.controller = controller;
			HBox.setHgrow(this, Priority.ALWAYS);
			controller.type = add(FxmlBuilder.choiceBox("mcpTransportType"));
			controller.type.getItems().addAll(McpTransportType.values());

			controller.url = add(new JFXTextField(), true);
			controller.url.setId("url");
			controller.url.setPromptText("url");

		}
	}
	

}
