package milkman.plugin.test.editor;

import com.jfoenix.controls.JFXTreeView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.TestResultAspect;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.utils.javafx.SettableTreeItem;

import java.util.LinkedList;

import static milkman.utils.fxml.FxmlBuilder.hbox;

public class TestResponseAspectEditor implements ResponseAspectEditor {

	JFXTreeView<Node> resultView;
	private SettableTreeItem<Node> root;
	private ObservableList<TreeItem<Node>> resultList;


	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		var testResultAspect = response.getAspect(TestResultAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("missing test result aspect"));


		resultList = FXCollections.observableList(new LinkedList<>());

		testResultAspect.getResults()
				.subscribe(evt -> resultList.add(new TreeItem<>(new Label(evt.getRequestName() + ": " + evt.getResultState()))));

		root = new SettableTreeItem<Node>();
		root.setChildren(resultList);


		var editor = new TestResponseAspectEditorFxml(this);
		return editor;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(TestResultAspect.class).isPresent();
	}

	public static class TestResponseAspectEditorFxml extends Tab {
		private final TestResponseAspectEditor controller;

		public TestResponseAspectEditorFxml(TestResponseAspectEditor controller) {
			super("Results");
			this.controller = controller;
			controller.resultView = new JFXTreeView<>();
			controller.resultView.setShowRoot(false);
			controller.resultView.setRoot(controller.root);

			var splitPane = new SplitPane();
			splitPane.setDividerPositions(0.2);

			splitPane.getItems().add(controller.resultView);
			splitPane.getItems().add(hbox());


			setContent(splitPane);
		}
	}


}
