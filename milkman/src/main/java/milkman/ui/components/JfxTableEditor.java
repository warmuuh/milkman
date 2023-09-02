package milkman.ui.components;

import static milkman.utils.fxml.facade.FxmlBuilder.button;

import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTableCell;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.javafx.JavaFxUtils;


@Slf4j
public class JfxTableEditor<T> extends StackPane {

	private final String tableId;

	private final MFXLegacyTableView<T> table = new MFXLegacyTableView<T>();

	private ObservableList<T> obsWrappedItems;

	private final Button addItemBtn;

	private final List<CustomAction> customActions = new LinkedList<>();

	private Function<T, String> rowToStringConverter;

	private Function<String, T> stringToRowConverter;

	private Integer firstEditableColumn;

	private Supplier<T> newItemCreator;


	public JfxTableEditor(String tableId) {
		this.tableId = tableId;
		table.setEditable(true);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		JavaFxUtils.publishEscToParent(table);
		getChildren().add(table);
		addItemBtn = button();
		addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLUS, "1.5em"));
		addItemBtn.getStyleClass().add("btn-add-entry");
		StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));
		getChildren().add(addItemBtn);

		KeyCombination keyCodeCopy = PlatformUtil.getControlKeyCombination(KeyCode.C);
		KeyCombination keyCodePaste = PlatformUtil.getControlKeyCombination(KeyCode.V);
		table.setOnKeyPressed(event -> {
			if (keyCodeCopy.match(event)) {
				copySelectionToClipboard();
			}
			if (keyCodePaste.match(event)) {
				pasteSelectionFromClipboard();
			}
		});
	}


	private void copySelectionToClipboard() {
		if (rowToStringConverter == null)
			return;
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (T treeItm : table.getSelectionModel().getSelectedItems()) {
			if (!first)
				b.append(System.lineSeparator());
			first = false;
			b.append(rowToStringConverter.apply(treeItm));
		}

		ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(b.toString());
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}


	private void pasteSelectionFromClipboard() {
		if (stringToRowConverter == null)
			return;

		String content = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
		if (content != null) {
			String[] lines = content.split("\\r?\\n");
			try {
				for (String line : lines) {
					T newEntry = stringToRowConverter.apply(line);
					if (newEntry != null) {
						obsWrappedItems.add(newEntry);
					}
				}
			} catch (Throwable t) {
				log.error("Failed to parse clipboard content: {}", t.getMessage());
			}

		}


	}



	/**
	 * used for converting selected rows to clipboard content
	 *
	 * @param rowToStringConverter
	 */
	public void setRowToStringConverter(Function<T, String> rowToStringConverter) {
		this.rowToStringConverter = rowToStringConverter;
	}

	public void setStringToRowConverter(Function<String, T> stringToRowConverter) {
		this.stringToRowConverter = stringToRowConverter;
	}


	public void addReadOnlyColumn(String name, Function<T, String> getter) {
		TableColumn<T, String> column = new TableColumn<>(name);
		var columnIdx = table.getColumns().size();
		loadPrefWidthOfColumn(column, columnIdx);
		column.setCellFactory((TableColumn<T, String> param) -> {
			return new GenericEditableTableCell<T, String>(new SelectableTextFieldBuilder());
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, (e, o) -> {}, param.getValue()));


		table.getColumns().add(column);
	}

	public void addColumn(String name, Function<T, String> getter, BiConsumer<T, String> setter) {
		TableColumn<T, String> column = new TableColumn<>(name);
		var columnIdx = table.getColumns().size();
		loadPrefWidthOfColumn(column, columnIdx);
		column.setCellFactory((TableColumn<T, String> param) -> {
			var cell = new GenericEditableTableCell<T, String>(new TextFieldEditorBuilderPatch());
			cell.setStepFunction(getStepFunction());
			return cell;
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue()));
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
		if (firstEditableColumn == null)
			firstEditableColumn = table.getColumns().size() -1;
	}


	//returns the number of rows to advance.
	protected BiFunction<Integer, Integer, Integer> getStepFunction() {
		return (index, direction) -> {
			if (obsWrappedItems.size()-1 == index && direction > 0) {
				var newItemAdded = addNewItem();
				if (newItemAdded) {
					Platform.runLater(() -> {
						if (firstEditableColumn != null)
							table.edit(index+direction, table.getColumns().get(firstEditableColumn));
					});
				}
				return newItemAdded ? direction : 0;
			}
			return direction;
		};
	}

	public void addColumn(String name, Function<T, String> getter, BiConsumer<T, String> setter, Consumer<TextField> textFieldInitializer) {
		TableColumn<T, String> column = new TableColumn<>(name);
		var columnIdx = table.getColumns().size();
		loadPrefWidthOfColumn(column, columnIdx);
		column.setCellFactory((TableColumn<T, String> param) -> {
			var cell = new GenericEditableTableCell<T, String>(new InitializingCellBuilder(textFieldInitializer));
			cell.setStepFunction(getStepFunction());
			return cell;
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue()));
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
		if (firstEditableColumn == null)
			firstEditableColumn = table.getColumns().size() -1;
	}


	public void addCheckboxColumn(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		TableColumn<T, Boolean> column = new TableColumn<>(name);
		var columnIdx = table.getColumns().size();
		loadPrefWidthOfColumn(column, columnIdx);
		column.setCellValueFactory(param -> {
			return GenericBinding.of(getter, setter, param.getValue());
		});
		column.setCellFactory(param -> new BooleanCell<>(column));
		column.setEditable(false);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	public void addDeleteColumn(String name) {
		addDeleteColumn(name, null);
	}

	public void addDeleteColumn(String name, Consumer<T> listener) {
		TableColumn<T, String> column = new TableColumn<>(name);
		var columnIdx = table.getColumns().size();
		loadPrefWidthOfColumn(column, columnIdx);
		column.setCellFactory(c -> new CustomActionsCell());
		column.setEditable(false);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);

		customActions.add(new CustomAction(FontAwesomeIcon.TIMES, (item) -> {
			obsWrappedItems.remove(item);
			if (listener != null) {
				listener.accept(item);
			}
		}));
	}


	private <O>  void loadPrefWidthOfColumn(TableColumn<T, O> column, int columnIdx) {
		CoreApplicationOptionsProvider.options().getUiPrefs()
				.getPrefWidth(tableId, columnIdx)
				.ifPresent(column::setPrefWidth);
		column.widthProperty().addListener((obs, o, n) -> {
			CoreApplicationOptionsProvider.options().getUiPrefs().setWidthForColumn(tableId, columnIdx, n.intValue());
		});
	}




	public void enableAddition(Supplier<T> newItemCreator) {
		this.newItemCreator = newItemCreator;
		addItemBtn.setVisible(true);
		addItemBtn.setOnAction(e -> {
			addNewItem();
		});
	}


	protected boolean addNewItem() {
		if (newItemCreator != null) {
			var newItem = newItemCreator.get();
			if (newItem != null) {
				obsWrappedItems.add(newItem);
				return true;
			}
		}
		return false;
	}

	public void addNewItemManually(T newItem){
		obsWrappedItems.add(newItem);
		Platform.runLater(() -> {
			table.refresh();
		});
	}

	public void disableAddition() {
		addItemBtn.setVisible(false);
	}

	public void addCustomAction(FontAwesomeIcon icon, Consumer<T> action){
		customActions.add(new CustomAction(icon, action::accept));
	}

	public void setItems(List<T> items) {
		setItems(items, null);
	}
	public void setItems(List<T> items, Comparator<T> comparator) {
		obsWrappedItems = FXCollections.observableList(items);
		if (comparator != null) {
			FXCollections.sort(obsWrappedItems, (ra, rb) -> comparator.compare(ra, rb));
		}

		table.setItems(obsWrappedItems);

		//register double-click listener for empty rows, to add a new instance
		this.table.setRowFactory(view -> {
		    TableRow<T> row = new TableRow<T>();
		    row.setOnMouseClicked(event -> {
		    	if (newItemCreator != null) {
					if (row.isEmpty() && (event.getClickCount() == 2)) {
						T myItem = newItemCreator.get();
						table.getItems().add(myItem);
					}
				}
			});
		    return row;
		});

		//we cant click on row if there is no row, so we have to register another event handler
		//for when the table is empty
		this.table.setOnMouseClicked(event -> {
			if (newItemCreator != null) {
				if (table.getItems().isEmpty() && (event.getClickCount() == 2)) {
					T myItem = newItemCreator.get();
					table.getItems().add(myItem);
				}
			}
		});

	}

	public void clearContent() {
		table.getColumns().clear();
	}



	private final class CustomActionsCell extends TableCell<T, String> {

		private Button createButton(CustomAction action) {
			Button btn = button();
			btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			btn.setGraphic(new FontAwesomeIconView(action.getIcon(), "1.5em"));
			btn.setOnAction(event -> {
				T wrappedItem = getTableRow().getItem();
				action.getAction().accept(wrappedItem);
				Platform.runLater(getTableView()::refresh);

			});
			return btn;
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				var hbox = new HBox();
				customActions.stream()
						.map(this::createButton)
						.forEach(hbox.getChildren()::add);
				hbox.setAlignment(Pos.CENTER);
				setGraphic(hbox);
				setText(null);
			}
		}
	}




	public class BooleanCell<T2> extends TableCell<T2, Boolean> {
		private final CheckBox checkBox;

		public BooleanCell(TableColumn<T, ?> column) {
			checkBox = new CheckBox();

//            checkBox.setDisable(true);
			checkBox.setOnAction(e -> {
				var row = getTableRow().getIndex();
				table.edit(row, column);
//        			itemProperty().setValue(newValue == null ? false : newValue);

				//we cannot use commitEdit because we would need to set column editable
				// but we dont want this as <tab> should not select this column,
//					commitEdit(!checkBox.isSelected());
				//hacky way to set value without column being editable:
				GenericBinding<T2, Boolean> binding = (GenericBinding<T2, Boolean>) column.getCellObservableValue(row);
				binding.set(checkBox.isSelected());
			});
			setGraphic(checkBox);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			setEditable(true);
		}

		@Override
		public void updateItem(Boolean item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setGraphic(null);
			} else {
				checkBox.setSelected(item);
				var hbox = new HBox(checkBox);
				hbox.setAlignment(Pos.CENTER);
				setGraphic(hbox);
			}
		}
	}


	public static class TextFieldEditorBuilderPatch extends TextFieldEditorBuilder {


		@Override
		public void startEdit() {
			Platform.runLater(() -> {
				if (textField != null) { //added nullcheck
					textField.selectAll();
					textField.requestFocus();
				}
			});
		}


		@Override
		public void updateItem(String item, boolean empty) {
			Platform.runLater(() -> {
				if (textField != null) { //added nullcheck
					textField.selectAll();
					textField.requestFocus();
				}
			});
		}
	}

	public static class SelectableTextFieldBuilder extends TextFieldEditorBuilderPatch {

		@Override
		public Region createNode(String value, EventHandler<KeyEvent> keyEventsHandler,
								 ChangeListener<Boolean> focusChangeListener) {
			Region node = super.createNode(value, keyEventsHandler, focusChangeListener);

			textField.setEditable(false);

			return node;
		}


	}

	public void setStringToRowConverter(Object stringToRowConverter2) {
		// TODO Auto-generated method stub

	}


	@Value
	private class CustomAction {
		FontAwesomeIcon icon;
		Consumer<T> action;
	}

}
