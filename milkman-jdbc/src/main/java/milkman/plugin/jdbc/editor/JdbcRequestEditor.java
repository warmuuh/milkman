package milkman.plugin.jdbc.editor;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;

public class JdbcRequestEditor implements RequestTypeEditor, AutoCompletionAware {

	
	 TextField jdbcUrl;
	
	private GenericBinding<JdbcRequestContainer, String> urlBinding = GenericBinding.of(JdbcRequestContainer::getJdbcUrl, JdbcRequestContainer::setJdbcUrl);

	private AutoCompleter completer;
	
	
	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new JdbcRequestEditorFxml(this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof JdbcRequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");
		
		JdbcRequestContainer restRequest = (JdbcRequestContainer)request;
		urlBinding.bindTo(jdbcUrl.textProperty(), restRequest);
		urlBinding.addListener(s -> request.setDirty(true));
		completer.attachVariableCompletionTo(jdbcUrl);
		
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}

	public static class JdbcRequestEditorFxml extends HboxExt {
		private JdbcRequestEditor controller; //avoid gc collection

		public JdbcRequestEditorFxml(JdbcRequestEditor controller) {
			this.controller = controller;
			HBox.setHgrow(this, Priority.ALWAYS);
			controller.jdbcUrl = add(new JFXTextField(), true);
			controller.jdbcUrl.setId("jdbcUrl");
			controller.jdbcUrl.setPromptText("jdbc:type://host[:port][/database][?parameters]");
		}
	}
	

}
