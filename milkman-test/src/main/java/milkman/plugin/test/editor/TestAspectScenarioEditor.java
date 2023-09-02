package milkman.plugin.test.editor;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.fxml.facade.FxmlBuilder;

import static milkman.utils.fxml.facade.FxmlBuilder.VboxExt;

@Slf4j
public class TestAspectScenarioEditor implements RequestAspectEditor {

	private Toggle cbFailOnFirst;
	private Toggle cbPropagateEnvironment;

	private final GenericBinding<TestAspect, Boolean> stopOnFirstFailureBinding = GenericBinding.of(TestAspect::isStopOnFirstFailure, TestAspect::setStopOnFirstFailure);
	private final GenericBinding<TestAspect, Boolean> propagateEnvBinding = GenericBinding.of(TestAspect::isPropagateResultEnvironment, TestAspect::setPropagateResultEnvironment);


	@Override
	public Tab getRoot(RequestContainer request) {
		TestAspect testAspect = request.getAspect(TestAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("missing test aspect"));

		var content = new TestAspectScenarioEditorFxml(this);

		stopOnFirstFailureBinding.bindTo(cbFailOnFirst.selectedProperty(), testAspect);
		stopOnFirstFailureBinding.addListener(c -> request.setDirty(true));

		propagateEnvBinding.bindTo(cbPropagateEnvironment.selectedProperty(), testAspect);
		propagateEnvBinding.addListener(c -> request.setDirty(true));

		return new Tab("Scenario", content);
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(TestAspect.class).isPresent();
	}




	public static class TestAspectScenarioEditorFxml extends VboxExt {
		private final TestAspectScenarioEditor controller;

		public TestAspectScenarioEditorFxml(TestAspectScenarioEditor controller) {
			this.controller = controller;
			add(new Label("Scenario Options"));
			controller.cbFailOnFirst = add(FxmlBuilder.toggle("Stop on first failure"));
			controller.cbPropagateEnvironment = add(FxmlBuilder.toggle("Propagate environment changes"));
			getStyleClass().add("generic-content-pane");

		}
	}



}
