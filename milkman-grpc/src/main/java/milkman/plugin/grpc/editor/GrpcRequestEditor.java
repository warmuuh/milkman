package milkman.plugin.grpc.editor;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;

public class GrpcRequestEditor implements RequestTypeEditor, AutoCompletionAware {


	 TextField endpoint;
	
	private GenericBinding<GrpcRequestContainer, String> endpointBinding = GenericBinding.of(GrpcRequestContainer::getEndpoint, GrpcRequestContainer::setEndpoint);
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
		
		GrpcRequestContainer grpcRequest = (GrpcRequestContainer)request;
		
		endpointBinding.bindTo(endpoint.textProperty(), grpcRequest);
		endpointBinding.addListener(s -> request.setDirty(true));
		
		
		completer.attachVariableCompletionTo(endpoint);
	}


	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
		
	}

	
	public static class GrpcRequestEditControllerFxml extends HboxExt {
		public GrpcRequestEditControllerFxml(GrpcRequestEditor controller) {
			HBox.setHgrow(this, Priority.ALWAYS);
			
			controller.endpoint = add(new JFXTextField(), true);
			controller.endpoint.setId("endpoint");
		}
	}
}
