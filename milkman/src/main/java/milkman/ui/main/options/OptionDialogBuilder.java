package milkman.ui.main.options;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import com.jfoenix.controls.JFXToggleButton;

import io.vavr.Function1;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import milkman.utils.fxml.GenericBinding;

public class OptionDialogBuilder  {

	public interface OptionPaneBuilder<T> {
		public OptionPaneBuilder<T> toggle(String name, Function1<T, Boolean> getter, BiConsumer<T, Boolean> setter);
		public OptionSectionBuilder<T> endSection();
	}
	
	public interface OptionSectionBuilder<T> {
		public OptionPaneBuilder<T> section(String name);
		public OptionDialogPane build();
	}
	
	@RequiredArgsConstructor
	public static class OptionPageBuilder<T> implements OptionPaneBuilder<T>, OptionSectionBuilder<T> {

		
		private final String name;
		private final T optionsObject;
		private List<Node> nodes = new LinkedList<Node>();
		private List<GenericBinding<?, ?>> bindings = new LinkedList<>();
		
		@Override
		public OptionPaneBuilder<T> section(String name) {
			Label lbl = new Label(name);
			lbl.getStyleClass().add("section-header");
			nodes.add(lbl);
			return this;
		}

		@Override
		public OptionPaneBuilder<T> toggle(String name, Function1<T, Boolean> getter, BiConsumer<T, Boolean> setter) {

			JFXToggleButton button = new JFXToggleButton();
			button.setText(name);
			GenericBinding<T,Boolean> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			button.selectedProperty().bindBidirectional(binding);
			nodes.add(button);
			return this;
		}

		@Override
		public OptionSectionBuilder<T> endSection() {
			return this;
		}
		
		@Override
		public OptionDialogPane build() {
			OptionDialogPane pane = new OptionDialogPane(name, bindings);
			pane.getStyleClass().add("options-page");
			pane.getChildren().addAll(nodes);
			return pane;
		}
	}
	
	
	public <T> OptionSectionBuilder<T> page(String name, T optionBean){
		return new OptionPageBuilder(name, optionBean);
	}
	
}
