package milkman.ui.plugin.rest;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.MainEditingArea;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.utils.fxml.GenericBinding;

public class RestRequestEditController implements MainEditingArea {

	@FXML TextField requestUrl;
	@FXML ChoiceBox<String> httpMethod;
	
	private GenericBinding<RestRequestContainer, String> urlBinding = GenericBinding.of(RestRequestContainer::getUrl, RestRequestContainer::setUrl);
	private GenericBinding<RestRequestContainer, String> httpMethodBinding = GenericBinding.of(RestRequestContainer::getHttpMethod, RestRequestContainer::setHttpMethod);
	
	
	@Override
	@SneakyThrows
	public Node getRoot() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/rest/MainEditorArea.fxml"));
		loader.setControllerFactory(c -> this);
		Node root = loader.load();
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof RestRequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");
		
		RestRequestContainer restRequest = (RestRequestContainer)request;
		
		urlBinding.bindTo(requestUrl.textProperty(), restRequest);
		httpMethodBinding.bindTo(httpMethod.valueProperty(), restRequest);
	}

	@FXML public void onClick() {}

}
