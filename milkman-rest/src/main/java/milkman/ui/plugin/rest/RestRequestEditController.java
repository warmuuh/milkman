package milkman.ui.plugin.rest;

import static milkman.utils.fxml.FxmlBuilder.icon;

import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.components.LongTextField;
import milkman.ui.main.dialogs.SelectValueDialog;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.rest.HttpOptionsPluginProvider.HttpOptions;
import milkman.ui.plugin.rest.domain.RestQueryParamAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;
import org.apache.commons.lang3.StringUtils;

public class RestRequestEditController implements RequestTypeEditor, AutoCompletionAware {

	public static final String NO_CERTIFICATE_VALUE = "No Certificate";
	TextField requestUrl;
	 JFXComboBox<String> httpMethod;

	private GenericBinding<RestRequestContainer, String> urlBinding = GenericBinding.of(RestRequestContainer::getUrl, RestRequestContainer::setUrl);
	private GenericBinding<RestRequestContainer, String> httpMethodBinding = GenericBinding.of(RestRequestContainer::getHttpMethod, RestRequestContainer::setHttpMethod);
	private GenericBinding<RestRequestContainer, String> certificateBinding = GenericBinding.of(RestRequestContainer::getClientCertificate, RestRequestContainer::setClientCertificate);

	private Property<String> certificateValue = new SimpleStringProperty();

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

		certificateBinding.bindTo(certificateValue, restRequest);
		urlBinding.bindTo(requestUrl.textProperty(), restRequest);
		httpMethodBinding.bindTo(httpMethod.valueProperty(), restRequest);

		urlBinding.addListener(s -> request.setDirty(true));
		httpMethodBinding.addListener(s -> request.setDirty(true));
		certificateBinding.addListener(s -> request.setDirty(true));
		
		//bind to param tab:
		request.onInvalidate.clear();
		request.onInvalidate.add(() -> requestUrl.setText(restRequest.getUrl())); //contract with query-param tab
		request.getAspect(RestQueryParamAspect.class).ifPresent(a -> {
			a.parseQueryParams(requestUrl.getText());
			a.linkToUrlTextfield(requestUrl.textProperty());
		});
		completer.attachVariableCompletionTo(requestUrl);
	}


	private void editRequestProperties() {
		SelectValueDialog dialog = new SelectValueDialog();

		List<String> values = new LinkedList<>();
		values.add(NO_CERTIFICATE_VALUE);
		HttpOptionsPluginProvider.options().getCertificates().forEach(c -> values.add(c.getName()));

		String certVal = certificateValue.getValue();
		if (StringUtils.isEmpty(certVal)) {
			certVal = NO_CERTIFICATE_VALUE;
		}

		dialog.showAndWait("Request Properties",
				"Client Certificate",
				Optional.of(certVal),
				values);

		if (!dialog.isCancelled()) {
			if (NO_CERTIFICATE_VALUE.equals(dialog.getInput())) {
				certificateValue.setValue(null);
			} else {
				certificateValue.setValue(dialog.getInput());
			}
		}
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
		
	}

	
	public static class RestRequestEditControllerFxml extends HboxExt {
		public RestRequestEditControllerFxml(RestRequestEditController controller) {
			HBox.setHgrow(this, Priority.ALWAYS);

			var icon = add(icon(FontAwesomeIcon.COG));
			icon.setOnMouseClicked(e -> controller.editRequestProperties());

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
			
			controller.requestUrl = add(new LongTextField(), true);
			controller.requestUrl.setId("requestUrl");
		}
	}

}
