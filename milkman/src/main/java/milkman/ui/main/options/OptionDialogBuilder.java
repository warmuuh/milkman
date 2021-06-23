package milkman.ui.main.options;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import milkman.ui.components.JfxTableEditor;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.fxml.facade.FxmlBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class OptionDialogBuilder  {

	public interface OptionPaneBuilder<T> {
		OptionPaneBuilder<T> toggle(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter);
		OptionPaneBuilder<T> passwordInput(String name, Function<T, String> getter, BiConsumer<T, String> setter);
		OptionPaneBuilder<T> textInput(String name, Function<T, String> getter, BiConsumer<T, String> setter);
		OptionPaneBuilder<T> numberInput(String name, Function<T, Integer> getter, BiConsumer<T, Integer> setter);
		OptionPaneBuilder<T> button(String name, Runnable runnable);
		OptionPaneBuilder<T> list(Function<T, List<String>> itemProvider);

		OptionPaneBuilder<T> selection(String name, Function<T, String> getter, BiConsumer<T, String> setter, List<String> possibleValues);
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
		private final List<Node> nodes = new LinkedList<Node>();
		private final List<GenericBinding<?, ?>> bindings = new LinkedList<>();
		
		@Override
		public OptionPaneBuilder<T> section(String name) {
			Label lbl = new Label(name);
			lbl.getStyleClass().add("section-header");
			nodes.add(lbl);
			return this;
		}

		@Override
		public OptionPaneBuilder<T> toggle(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {

			ToggleButton button = FxmlBuilder.toggle(name);
			GenericBinding<T,Boolean> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			button.selectedProperty().bindBidirectional(binding);
			HBox hbox = new HBox(button);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}

		@Override
		public OptionPaneBuilder<T> textInput(String name, Function<T, String> getter, BiConsumer<T, String> setter) {

			Label lbl = new Label(name);
			TextField text = FxmlBuilder.text();
			GenericBinding<T,String> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			text.textProperty().bindBidirectional(binding);
			HBox hbox = new HBox(lbl, text);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}
		

		@Override
		public OptionPaneBuilder<T> passwordInput(String name, Function<T, String> getter, BiConsumer<T, String> setter) {

			Label lbl = new Label(name);
			TextField text = FxmlBuilder.password();
			GenericBinding<T,String> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			text.textProperty().bindBidirectional(binding);
			HBox hbox = new HBox(lbl, text);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}
		
		@Override
		public OptionPaneBuilder<T> numberInput(String name, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {

			Label lbl = new Label(name);
			var text = FxmlBuilder.vtext();
			text.setValidators(FxmlBuilder.integerValidator("Not an integer"));
			
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
		public OptionPaneBuilder<T> selection(String name, Function<T, String> getter, BiConsumer<T, String> setter,
				List<String> possibleValues) {
			
			Label lbl = new Label(name);
			ComboBox<String> text = FxmlBuilder.combobox();
			GenericBinding<T,String> binding = GenericBinding.of(getter, setter, optionsObject);
			bindings.add(binding);
			text.valueProperty().bindBidirectional(binding);
			text.getItems().addAll(possibleValues);
			HBox hbox = new HBox(lbl, text);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}

		@Override
		public OptionPaneBuilder<T> button(String name, Runnable runnable) {
			Button btn = FxmlBuilder.button(name);
			btn.setOnAction(e -> runnable.run());
			btn.getStyleClass().add("secondary-button");
			HBox hbox = new HBox(btn);
			hbox.getStyleClass().add("options-entry");
			nodes.add(hbox);
			return this;
		}

		@Override
		public OptionPaneBuilder<T> list(Function<T, List<String>> itemProvider) {
			List<String> items = itemProvider.apply(optionsObject);
			List<Entry<Integer, String>> zipWithIndex = new LinkedList<>();
			for (int i = 0; i < items.size(); i++) {
				zipWithIndex.add(new HashMap.SimpleEntry<>(i, items.get(i)));
			}
			JfxTableEditor<Entry<Integer, String>> table = new JfxTableEditor<>();
			table.addColumn("Script Url", Entry::getValue, (e, v) -> {
				e.setValue(v);
				items.set(e.getKey(), v);
			});
			table.enableAddition(() -> {
				items.add("");
				return new HashMap.SimpleEntry<>(items.size()-1, "");
			});
			table.addDeleteColumn("remove", removed -> items.remove((int)removed.getKey()));
			table.setItems(zipWithIndex);
			nodes.add(table);
			return this;
		}


	}
	
	
	public <T> OptionSectionBuilder<T> page(String name, T optionBean){
		return new OptionPageBuilder(name, optionBean);
	}
	
}
