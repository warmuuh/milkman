package milkman.plugin.test.editor;

import com.jfoenix.controls.JFXTreeView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
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
import static milkman.utils.fxml.FxmlBuilder.icon;

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
				.subscribe(this::updateResultList);

		root = new SettableTreeItem<Node>();
		root.setChildren(resultList);


		var editor = new TestResponseAspectEditorFxml(this);
		return editor;
	}

	private void updateResultList(TestResultAspect.TestResultEvent evt) {

		var evtTypeIcon = FontAwesomeIcon.CLOCK_ALT;
		switch (evt.getResultState()){
			case STARTED:
				evtTypeIcon = FontAwesomeIcon.CLOCK_ALT;
				break;
			case SUCCEEDED:
				evtTypeIcon = FontAwesomeIcon.CHECK_CIRCLE_ALT;
				break;
			case FAILED:
				evtTypeIcon = FontAwesomeIcon.BAN;
				break;
			case EXCEPTION:
				evtTypeIcon = FontAwesomeIcon.EXCLAMATION_CIRCLE;
				break;
		}



		var treeItem = hbox(icon(evtTypeIcon, "1.5em"), new Label(evt.getRequestName()));
		treeItem.setUserData(evt.getRequestId());

		boolean updated = false;
		for (TreeItem<Node> item : resultList) {
			if (item.getValue().getUserData().equals(evt.getRequestId())) {
				item.setValue(treeItem);
				updated = true;
				break;
			}
		}

		if (!updated) {
			resultList.add(new TreeItem<>(treeItem));
		}
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
