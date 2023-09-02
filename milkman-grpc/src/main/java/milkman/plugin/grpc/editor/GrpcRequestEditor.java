package milkman.plugin.grpc.editor;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.fxml.facade.FxmlBuilder;
import milkman.utils.fxml.facade.FxmlBuilder.HboxExt;
import milkman.utils.fxml.facade.SimpleToggleButton;

public class GrpcRequestEditor implements RequestTypeEditor, AutoCompletionAware {


	TextField endpoint;
	SimpleToggleButton useTls;

	private final GenericBinding<GrpcRequestContainer, String> endpointBinding = GenericBinding.of(GrpcRequestContainer::getEndpoint, GrpcRequestContainer::setEndpoint);
	private final GenericBinding<GrpcRequestContainer, Boolean> tlsBinding = GenericBinding.of(GrpcRequestContainer::isUseTls, GrpcRequestContainer::setUseTls);

	private AutoCompleter completer;


	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new GrpcRequestEditControllerFxml(this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof GrpcRequestContainer))
			throw new IllegalArgumentException("Unsupported request type");

		GrpcRequestContainer grpcRequest = (GrpcRequestContainer) request;


		tlsBinding.bindTo(useTls.selectedProperty(), grpcRequest);
		tlsBinding.addListener(s -> request.setDirty(true));

		endpointBinding.bindTo(endpoint.textProperty(), grpcRequest);
		endpointBinding.addListener(s -> request.setDirty(true));


		completer.attachVariableCompletionTo(endpoint);
	}


	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;

	}


	public static class GrpcRequestEditControllerFxml extends HboxExt {
		private final GrpcRequestEditor controller; //avoid gc collection

		public GrpcRequestEditControllerFxml(GrpcRequestEditor controller) {
			this.controller = controller;
			HBox.setHgrow(this, Priority.ALWAYS);


			controller.useTls = add(FxmlBuilder.toggle("TLS"));

			controller.endpoint = add(FxmlBuilder.text(), true);
			controller.endpoint.setPromptText("host:port");
			controller.endpoint.setId("endpoint");
		}
	}
}
