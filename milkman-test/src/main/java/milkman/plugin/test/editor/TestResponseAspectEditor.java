package milkman.plugin.test.editor;

import static milkman.utils.fxml.facade.FxmlBuilder.VboxExt;
import static milkman.utils.fxml.facade.FxmlBuilder.hbox;
import static milkman.utils.fxml.facade.FxmlBuilder.icon;
import static milkman.utils.fxml.facade.FxmlBuilder.treeView;
import static milkman.utils.fxml.facade.FxmlBuilder.vbox;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.LinkedList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.TestResultAspect;
import milkman.plugin.test.domain.TestResultAspect.TestResultEvent;
import milkman.plugin.test.domain.TestResultAspect.TestResultState;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.utils.javafx.SettableTreeItem;

public class TestResponseAspectEditor implements ResponseAspectEditor {

	TreeView<Node> resultView;
	private SettableTreeItem<Node> root;
	private ObservableList<TreeItem<Node>> resultList;
	private VboxExt resultDetails;


	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		var testResultAspect = response.getAspect(TestResultAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("missing test result aspect"));


		resultList = FXCollections.observableList(new LinkedList<>());

		root = new SettableTreeItem<Node>();
		root.setChildren(resultList);

		testResultAspect.getResults()
				.subscribe(this::updateResultList);


		var editor = new TestResponseAspectEditorFxml(this);

		resultView.getSelectionModel().selectedIndexProperty().addListener((obs, old, newValue) -> {
			if (newValue != null && !newValue.equals(old)){
				onResultSelected((TestResultEvent) resultList.get(newValue.intValue()).getValue().getUserData());
			}
		});

		return editor;
	}


	private void updateResultList(TestResultEvent evt) {

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
			case IGNORED:
				evtTypeIcon = FontAwesomeIcon.LOW_VISION;
				break;
			case SKIPPED:
				evtTypeIcon = FontAwesomeIcon.FILTER;
				break;
			case EXCEPTION:
				evtTypeIcon = FontAwesomeIcon.EXCLAMATION_CIRCLE;
				break;
		}



		var treeItem = hbox(icon(evtTypeIcon, "1.5em"), new Label(evt.getRequestName()));
		treeItem.setUserData(evt);

		boolean updated = false;
		for (TreeItem<Node> item : resultList) {
			TestResultEvent oldEvent = (TestResultEvent) item.getValue().getUserData();
			//if already succeeded, this is a repeated run and we report it separately
			boolean succeededAlready = oldEvent.getResultState() == TestResultState.SUCCEEDED;
			if (!succeededAlready && oldEvent.getRequestId().equals(evt.getRequestId())) {
				item.setValue(treeItem);
				updated = true;
				break;
			}
		}

		if (!updated) {
			resultList.add(new TreeItem<>(treeItem));
		}
	}


	private void onResultSelected(TestResultEvent resultEvent) {
		resultDetails.getChildren().clear();

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
//		grid.setPadding(new Insets(25, 25, 25, 25));
		grid.getColumnConstraints().add(new ColumnConstraints(100));

		grid.add(new Label("Test Results for '" + resultEvent.getRequestName() + "'"), 0,0,2,1);
		grid.add(new Label("Outcome"), 0, 1);
		grid.add(new Label(resultEvent.getResultState().toString()), 1, 1);

		int curRow = 2;
		for (var entry : resultEvent.getDetails().entrySet()) {
			grid.add(new Label(entry.getKey()), 0, curRow);
			var valueLabel = new Label(entry.getValue().getText());
			if (!CoreApplicationOptionsProvider.options().isDisableColorfulUi()) {
				entry.getValue().getStyle().ifPresent(valueLabel::setStyle);
			}
			valueLabel.setWrapText(true);
			grid.add(valueLabel, 1, curRow);
			curRow += 1;
		}

		resultDetails.add(grid, true);
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
			controller.resultView = treeView();
			controller.resultView.setShowRoot(false);
			controller.resultView.setRoot(controller.root);

			var splitPane = new SplitPane();
			splitPane.setDividerPositions(0.2);

			splitPane.getItems().add(controller.resultView);

			controller.resultDetails = vbox();
			controller.resultDetails.getStyleClass().add("generic-content-pane");
			splitPane.getItems().add(controller.resultDetails);

			setContent(splitPane);
		}
	}


}
