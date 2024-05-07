package milkman.plugin.test.editor;

import com.jfoenix.controls.JFXTreeView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.StatusInfoContainer;
import milkman.plugin.test.domain.TestResultAspect;
import milkman.plugin.test.domain.TestResultAspect.TestResultEvent;
import milkman.plugin.test.domain.TestResultAspect.TestResultState;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.utils.CollectionUtils;
import milkman.utils.javafx.SettableTreeItem;

import java.util.LinkedList;

import static milkman.utils.fxml.FxmlBuilder.*;

public class TestResponseAspectEditor implements ResponseAspectEditor {

	JFXTreeView<Node> resultView;
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
		grid.getColumnConstraints().add(new ColumnConstraints(200));

		grid.add(new Label("Test Results for '" + resultEvent.getRequestName() + "'"), 0,0,2,1);
		grid.add(new Label("Outcome"), 0, 1);
		grid.add(new Label(resultEvent.getResultState().toString()), 1, 1);

		AtomicInteger curRow = new AtomicInteger(2);
		resultEvent.getDetails().ifPresent(details -> addStatusInformation(details, (key, value) -> {
			grid.add(key, 0, curRow.get());
			grid.add(value, 1, curRow.get());
			curRow.incrementAndGet();
		}));

		resultEvent.getError().ifPresent(errMsg -> {
			grid.add(new Label("Error"), 0, curRow.get());
			grid.add(new Label(errMsg), 1, curRow.get());
			curRow.incrementAndGet();
		});

		resultDetails.add(grid, true);
	}

	private void addStatusInformation(StatusInfoContainer statusInformations, BiConsumer<Label, Label> addRow) {
		statusInformations.subscribe(entry ->  Platform.runLater(() -> {
			if (entry.isGroup()) {
				entry.getValueMap().forEach((key, value) -> {
					addRow.accept(new Label(entry.getKey() + " - " + key), new Label(value));
				});
			} else {
				addRow.accept(new Label(entry.getKey()), new Label(entry.getValue().getText()));
			}
		}));
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

			controller.resultDetails = vbox();
			controller.resultDetails.getStyleClass().add("generic-content-pane");
			splitPane.getItems().add(controller.resultDetails);

			setContent(splitPane);
		}
	}


}
