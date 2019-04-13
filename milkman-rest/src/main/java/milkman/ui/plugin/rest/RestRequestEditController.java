package milkman.ui.plugin.rest;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXComboBox;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.rest.domain.RestQueryParamAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.GenericBinding;

public class RestRequestEditController implements RequestTypeEditor {

	@FXML TextField requestUrl;
	@FXML JFXComboBox<String> httpMethod;
	
	private GenericBinding<RestRequestContainer, String> urlBinding = GenericBinding.of(RestRequestContainer::getUrl, RestRequestContainer::setUrl);
	private GenericBinding<RestRequestContainer, String> httpMethodBinding = GenericBinding.of(RestRequestContainer::getHttpMethod, RestRequestContainer::setHttpMethod);
	
	
	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = FxmlUtil.loadAndInitialize("/rest/MainEditorArea.fxml", this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof RestRequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");
		
		RestRequestContainer restRequest = (RestRequestContainer)request;
		
		urlBinding.bindTo(requestUrl.textProperty(), restRequest);
		httpMethodBinding.bindTo(httpMethod.valueProperty(), restRequest);
		
		urlBinding.addListener(s -> request.setDirty(true));
		httpMethodBinding.addListener(s -> request.setDirty(true));
		
		
		//bind to param tab:
		request.onInvalidate.clear();
		request.onInvalidate.add(() -> requestUrl.setText(restRequest.getUrl())); //contract with query-param tab
		request.getAspect(RestQueryParamAspect.class).ifPresent(a -> a.linkToUrlTextfield(requestUrl.textProperty()));
		
	}

}
