package milkman.ui.main.options;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.IntegerValidator;

import io.vavr.Function1;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import milkman.utils.fxml.GenericBinding;

public class OptionDialogBuilder  {

	public interface OptionPaneBuilder<T> {
		OptionPaneBuilder<T> toggle(String name, Function1<T, Boolean> getter, BiConsumer<T, Boolean> setter);
		OptionPaneBuilder<T> textInput(String name, Function1<T, String> getter, BiConsumer<T, String> setter);
		OptionPaneBuilder<T> numberInput(String name, Function1<T, Integer> getter, BiConsumer<T, Integer> setter);

		OptionPaneBuilder<T> selection(String name, Function1<T, String> getter, BiConsumer<T, String> setter, List<String> possibleValues);
		OptionSectionBuilder<T> endSection();
	}
	
	public interface OptionSectionBuilder<T> {
		OptionPaneBuilder<T> section(String name);
		OptionDialogPane build();
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
			HBox hbox = new HBox(button);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}

		@Override
		public OptionPaneBuilder<T> textInput(String name, Function1<T, String> getter, BiConsumer<T, String> setter) {

			Label lbl = new Label(name);
			JFXTextField text = new JFXTextField();
			GenericBinding<T,String> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			text.textProperty().bindBidirectional(binding);
			HBox hbox = new HBox(lbl, text);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}
		
		@Override
		public OptionPaneBuilder<T> numberInput(String name, Function1<T, Integer> getter, BiConsumer<T, Integer> setter) {

			Label lbl = new Label(name);
			JFXTextField text = new JFXTextField();
			text.setValidators(new IntegerValidator("Not an integer"));
			
			BiConsumer<T, String> setFn = (obj, val) -> {
				if (text.validate())
					setter.accept(obj, Integer.parseInt(val));
			};
			GenericBinding<T,String> binding = GenericBinding.of(getter.andThen(Object::toString), setFn, optionsObject);
			bindings.add(binding);
			text.textProperty().bindBidirectional(binding);
			HBox hbox = new HBox(lbl, text);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}
		
		
		@Override
		public OptionSectionBuilder<T> endSection() {
			HBox hbox = new HBox();
			hbox.getStyleClass().add("section-end");
			nodes.add(hbox);
			return this;
		}
		
		@Override
		public OptionDialogPane build() {
			OptionDialogPane pane = new OptionDialogPane(name, bindings);
			pane.getStyleClass().add("options-page");
			pane.getChildren().addAll(nodes);
			return pane;
		}

		@Override
		public OptionPaneBuilder<T> selection(String name, Function1<T, String> getter, BiConsumer<T, String> setter,
				List<String> possibleValues) {
			
			Label lbl = new Label(name);
			JFXComboBox<String> text = new JFXComboBox();
			GenericBinding<T,String> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			text.valueProperty().bindBidirectional(binding);
			text.getItems().addAll(possibleValues);
			HBox hbox = new HBox(lbl, text);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}
	}
	
	
	public <T> OptionSectionBuilder<T> page(String name, T optionBean){
		return new OptionPageBuilder(name, optionBean);
	}
	
}
