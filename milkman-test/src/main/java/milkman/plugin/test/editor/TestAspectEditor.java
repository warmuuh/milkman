package milkman.plugin.test.editor;

import com.jfoenix.controls.JFXTreeView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.input.TransferMode;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.ui.plugin.PluginRequestExecutor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestExecutorAware;
import milkman.utils.javafx.MappedList;
import milkman.utils.javafx.SettableTreeItem;

import static milkman.utils.fxml.FxmlBuilder.HboxExt;
import static milkman.utils.fxml.FxmlBuilder.hbox;
import static milkman.utils.javafx.DndUtil.JAVA_FORMAT;
import static milkman.utils.javafx.DndUtil.deserialize;

@Slf4j
public class TestAspectEditor implements RequestAspectEditor, RequestExecutorAware {


	JFXTreeView<Node> requestSequence;
	private SettableTreeItem<Node> root;
	private ObservableList<String> requests;
	private ObservableList<TreeItem<Node>> mappedRequests;
	private PluginRequestExecutor requestExecutor;


	@Override
	public Tab getRoot(RequestContainer request) {
		TestAspect testAspect = request.getAspect(TestAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("missing test aspect"));


		requests = FXCollections.observableList(testAspect.getRequests());
		mappedRequests = new MappedList<>(requests, id -> {
			var treeItem = new SettableTreeItem<Node>();
			treeItem.setValue(requestIdToNode(id));
			return treeItem;
		});

		root = new SettableTreeItem<Node>();
		root.setChildren(mappedRequests);


		var editor = new TestAspectEditorFxml(this);
		setupDnD(requestSequence, testAspect);

		if (requests.size() > 0){
			//we have to do this in order for the component to refresh, otherwise, an empty treeView is rendered
			//bug in javafx?
			requests.add(0, requests.remove(0));
		}

		return editor;
	}

	private void setupDnD(JFXTreeView<Node> target, TestAspect testAspect) {
		target.setOnDragOver( e -> {
			if (e.getGestureSource() != target
			&& e.getDragboard().hasContent(JAVA_FORMAT)) {
				e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			e.consume();
		});

		target.setOnDragDropped(e -> {
			if (e.getGestureSource() != target
					&& e.getDragboard().hasContent(JAVA_FORMAT)) {
				try {
					var content = deserialize((String) e.getDragboard().getContent(JAVA_FORMAT), RequestContainer.class);
					requests.add(content.getId());
					testAspect.setDirty(true);
					e.setDropCompleted(true);
				} catch (Exception ex) {
					log.error("failed dnd operation", ex);
					e.setDropCompleted(false);
				}
				e.consume();
			}
		});
	}

	private HboxExt requestIdToNode(String requestId) {
		var requestName = requestExecutor.getDetails(requestId).get().getName();
		return hbox(new Label(requestName));
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(TestAspect.class).isPresent();
	}

	@Override
	public void setRequestExecutor(PluginRequestExecutor executor) {
		this.requestExecutor = executor;
	}

	public static class TestAspectEditorFxml extends Tab {
		private final TestAspectEditor controller;

		public TestAspectEditorFxml(TestAspectEditor controller) {
			super("Tests");
			this.controller = controller;
			controller.requestSequence = new JFXTreeView<>();
			controller.requestSequence.setShowRoot(false);
//			controller.requestSequence.setCellFactory(new DnDCellFactory());
			controller.requestSequence.setRoot(controller.root);

			var splitPane = new SplitPane();
			splitPane.setDividerPositions(0.2);

			splitPane.getItems().add(controller.requestSequence);
			splitPane.getItems().add(hbox());


			setContent(splitPane);
		}
	}





}
