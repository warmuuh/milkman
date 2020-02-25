package milkman.ui.plugin.rest;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.rest.domain.RestQueryParamAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;

public class RestRequestEditController implements RequestTypeEditor, AutoCompletionAware {

	 TextField requestUrl;
	 JFXComboBox<String> httpMethod;
	
	private GenericBinding<RestRequestContainer, String> urlBinding = GenericBinding.of(RestRequestContainer::getUrl, RestRequestContainer::setUrl);
	private GenericBinding<RestRequestContainer, String> httpMethodBinding = GenericBinding.of(RestRequestContainer::getHttpMethod, RestRequestContainer::setHttpMethod);
	private AutoCompleter completer;
	
	
	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new RestRequestEditControllerFxml(this);
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
		request.getAspect(RestQueryParamAspect.class).ifPresent(a -> {
			a.parseQueryParams(requestUrl.getText());
			a.linkToUrlTextfield(requestUrl.textProperty());
		});
		completer.attachVariableCompletionTo(requestUrl);
	}


	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
		
	}

	
	public static class RestRequestEditControllerFxml extends HboxExt {
		public RestRequestEditControllerFxml(RestRequestEditController controller) {
			HBox.setHgrow(this, Priority.ALWAYS);
			var methods = new JFXComboBox<String>();
			add(methods);
			methods.setId("httpMethods");
			controller.httpMethod = methods;
			methods.setValue("GET");
			methods.getItems().add("GET");
			methods.getItems().add("POST");
			methods.getItems().add("PUT");
			methods.getItems().add("DELETE");
			methods.getItems().add("PATCH");
			methods.getItems().add("HEAD");
			methods.getItems().add("OPTIONS");
			
			controller.requestUrl = add(new JFXTextField(), true);
			controller.requestUrl.setId("requestUrl");
		}
	}
	
}
