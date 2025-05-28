package milkman.ui.main.options;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.IntegerValidator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import milkman.ui.components.TableEditor;
import milkman.utils.fxml.GenericBinding;

public class OptionDialogBuilder  {

	public interface OptionPaneBuilder<T> {
		OptionPaneBuilder<T> toggle(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter);
		OptionPaneBuilder<T> passwordInput(String name, Function<T, String> getter, BiConsumer<T, String> setter);
		OptionPaneBuilder<T> textInput(String name, Function<T, String> getter, BiConsumer<T, String> setter);
		OptionPaneBuilder<T> numberInput(String name, Function<T, Integer> getter, BiConsumer<T, Integer> setter);
		OptionPaneBuilder<T> button(String name, Runnable runnable);
		OptionPaneBuilder<T> list(Function<T, List<String>> itemProvider);
		<V> OptionPaneBuilder<T> list(Function<T, List<V>> itemProvider,
				Map<String, Function<V, String>> columnValueProviders,
				Supplier<V> newValueProvider,
				Consumer<V> valueEditor);

		<V> OptionListBuilder<T, V> list();

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
		private final List<Node> nodes = new LinkedList<>();
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
		public OptionPaneBuilder<T> textInput(String name, Function<T, String> getter, BiConsumer<T, String> setter) {

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
		public OptionPaneBuilder<T> passwordInput(String name, Function<T, String> getter, BiConsumer<T, String> setter) {

			Label lbl = new Label(name);
			JFXPasswordField text = new JFXPasswordField();
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
		public OptionPaneBuilder<T> selection(String name, Function<T, String> getter, BiConsumer<T, String> setter,
				List<String> possibleValues) {

			Label lbl = new Label(name);
			JFXComboBox<String> text = new JFXComboBox<>();
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
			JFXButton btn = new JFXButton(name);
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
			TableEditor<Entry<Integer, String>> table = new TableEditor<>("options.list");
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

		public <V> OptionPaneBuilder<T> list(Function<T, List<V>> itemProvider,
				Map<String, Function<V, String>> columnValueProviders,
				Supplier<V> newValueProvider,
				Consumer<V> valueEditor
				) {
			List<V> items = itemProvider.apply(optionsObject);
			TableEditor<V> table = new TableEditor<>("options.generic.list");
			columnValueProviders.forEach(table::addReadOnlyColumn);
			table.enableAddition(newValueProvider);
			table.addDeleteColumn("remove", items::remove);
			table.addCustomAction(FontAwesomeIcon.EDIT, valueEditor);
			table.setItems(items);
			nodes.add(table);
			return this;
		}

		public <V> OptionListBuilder<T, V> list() {
			return new OptionListBuilder<>(this);
		}

		<V> OptionPaneBuilder<T> list(OptionListBuilder<T, V> listBuilder) {
			var items = listBuilder.itemProvider.apply(optionsObject);
			var table = new TableEditor<V>("options.generic.list");
			listBuilder.columnValueProviders.forEach(table::addReadOnlyColumn);
			table.enableAddition(listBuilder.newValueProvider);
			table.addDeleteColumn("remove", items::remove, listBuilder.changeListener);
			if (listBuilder.valueEditor != null) {
				table.addCustomAction(FontAwesomeIcon.EDIT, listBuilder.valueEditor);
			}
			table.setItems(items);
			nodes.add(table);
			return this;
		}

	}

	public <T> OptionSectionBuilder<T> page(String name, T optionBean){
		return new OptionPageBuilder<>(name, optionBean);
	}

	@RequiredArgsConstructor
	public static class OptionListBuilder<T, V> {

		private final OptionPageBuilder<T> parent;

		private Function<T, List<V>> itemProvider;
		private Map<String, Function<V, String>> columnValueProviders;
		private Supplier<V> newValueProvider;
		private Consumer<V> valueEditor;
		private VetoableListItemRemovalListener<V> changeListener;

		public OptionListBuilder<T, V> itemProvider(Function<T, List<V>> itemProvider) {
			this.itemProvider = itemProvider;
			return this;
		}

		public OptionListBuilder<T, V> columnValueProviders(Map<String, Function<V, String>> columnValueProviders) {
			this.columnValueProviders = columnValueProviders;
			return this;
		}

		public OptionListBuilder<T, V> newValueProvider(Supplier<V> newValueProvider) {
			this.newValueProvider = newValueProvider;
			return this;
		}

		public OptionListBuilder<T, V> valueEditor(Consumer<V> valueEditor) {
			this.valueEditor = valueEditor;
			return this;
		}

		public OptionListBuilder<T, V> vetoableListChangeListener(VetoableListItemRemovalListener<V> changeListener) {
			this.changeListener = changeListener;
			return this;
		}

		public OptionPaneBuilder<T> endList() {
			return parent.list(this);
		}
	}

}
