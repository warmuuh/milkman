package milkman.plugin.test.editor;

import static milkman.utils.FunctionalUtils.run;
import static milkman.utils.fxml.FxmlBuilder.*;
import static milkman.utils.fxml.FxmlBuilder.icon;

import com.jfoenix.controls.JFXTreeView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import milkman.domain.RequestContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.ui.main.RequestCollectionComponent;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.javafx.DnDCellFactory;
import milkman.utils.javafx.SettableTreeItem;

import javax.swing.*;

public class TestAspectEditor implements RequestAspectEditor {


	JFXTreeView<Node> requestSequence;
	private SettableTreeItem<Node> root;


	@Override
	public Tab getRoot(RequestContainer request) {
		TestAspect testAspect = request.getAspect(TestAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("missing test aspect"));



		var requests = FXCollections.<TreeItem<Node>>emptyObservableList();
		root = new SettableTreeItem<Node>();
		root.setChildren(requests);
		requestSequence.setRoot(root);

		return new TestAspectEditorFxml(this);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(TestAspect.class).isPresent();
	}

	public static class TestAspectEditorFxml extends Tab {
		private final TestAspectEditor controller;

		public TestAspectEditorFxml(TestAspectEditor controller) {
			super("Tests");
			this.controller = controller;
			controller.requestSequence = new JFXTreeView<>();
			controller.requestSequence.setShowRoot(false);
			controller.requestSequence.setCellFactory(new DnDCellFactory());
			controller.requestSequence.setRoot(controller.root);

			var splitPane = new SplitPane();
			splitPane.setDividerPositions(0.2);

			splitPane.getItems().add(controller.requestSequence);
			splitPane.getItems().add(hbox());


			setContent(splitPane);
		}
	}





}
