package milkman.plugin.cassandra.editor;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.utils.fxml.FxmlBuilder.HboxExt;
import milkman.utils.fxml.GenericBinding;

public class CassandraRequestEditor implements RequestTypeEditor, AutoCompletionAware {

	
	TextField cassandraUrl;
	
	private GenericBinding<CassandraRequestContainer, String> urlBinding = GenericBinding.of(CassandraRequestContainer::getCassandraUrl, CassandraRequestContainer::setCassandraUrl);

	private AutoCompleter completer;

	@Override
	@SneakyThrows
	public Node getRoot() {
		Node root = new CassandraRequestEditorFxml(this);
		return root;
	}

	@Override
	public void displayRequest(RequestContainer request) {
		if (!(request instanceof CassandraRequestContainer))
			throw new IllegalArgumentException("Other request types not yet supported");

		CassandraRequestContainer restRequest = (CassandraRequestContainer)request;
		
		urlBinding.bindTo(cassandraUrl.textProperty(), restRequest);
		urlBinding.addListener(s -> request.setDirty(true));
		completer.attachVariableCompletionTo(cassandraUrl);
		
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}

	public static class CassandraRequestEditorFxml extends HboxExt {
		private CassandraRequestEditor controller; //avoid gc collection

		public CassandraRequestEditorFxml(CassandraRequestEditor controller) {
			this.controller = controller;
			HBox.setHgrow(this, Priority.ALWAYS);
			controller.cassandraUrl = add(new JFXTextField(), true);
			controller.cassandraUrl.setId("cassandraUrl");
			controller.cassandraUrl.setPromptText("cql://host[:port][/keyspace]?dc=datacenter[&username=user&password=pass]");
		}
	}
	

}
