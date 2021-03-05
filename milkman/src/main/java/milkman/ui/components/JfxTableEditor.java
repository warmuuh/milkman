package milkman.ui.components;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.cells.editors.base.JFXTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.javafx.JavaFxUtils;
import milkman.utils.javafx.ResizableJfxTreeTableView;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
class RecursiveWrapper<T> extends RecursiveTreeObject<RecursiveWrapper<T>>{
	T data;
}

@Slf4j
public class JfxTableEditor<T> extends StackPane {
	
	
	private final ResizableJfxTreeTableView<RecursiveWrapper<T>> table = new ResizableJfxTreeTableView<RecursiveWrapper<T>>();

	private ObservableList<RecursiveWrapper<T>> obsWrappedItems;

	private final JFXButton addItemBtn;

	private final List<CustomAction> customActions = new LinkedList<>();

	private Function<T, String> rowToStringConverter;

	private Function<String, T> stringToRowConverter;
	
	private Integer firstEditableColumn;

	private Supplier<T> newItemCreator;


	public JfxTableEditor() {
		table.setShowRoot(false);
		table.setEditable(true);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		JavaFxUtils.publishEscToParent(table);
		getChildren().add(table);
		addItemBtn = new JFXButton();
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
		for (TreeItem<RecursiveWrapper<T>> treeItm : table.getSelectionModel().getSelectedItems()) {
			if (!first)
				b.append(System.lineSeparator());
			first = false;
			b.append(rowToStringConverter.apply(treeItm.getValue().getData()));	
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
						obsWrappedItems.add(new RecursiveWrapper<>(newEntry));
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
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory((TreeTableColumn<RecursiveWrapper<T>, String> param) -> {
			return new GenericEditableTreeTableCell<RecursiveWrapper<T>, String>(new SelectableTextFieldBuilder());
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, (e, o) -> {}, param.getValue().getValue().getData()));
		
		column.setMaxWidth(400);
		column.setMinWidth(100);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
		table.getColumns().add(column);
	}
	
	public void addColumn(String name, Function<T, String> getter, BiConsumer<T, String> setter) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory((TreeTableColumn<RecursiveWrapper<T>, String> param) -> {
			var cell = new GenericEditableTreeTableCell<RecursiveWrapper<T>, String>(new TextFieldEditorBuilderPatch());
			cell.setStepFunction(getStepFunction());
			return cell;
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue().getValue().getData()));
		column.setMaxWidth(400);
		column.setMinWidth(100);
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
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory((TreeTableColumn<RecursiveWrapper<T>, String> param) -> {
			var cell = new GenericEditableTreeTableCell<RecursiveWrapper<T>, String>(new InitializingCellBuilder(textFieldInitializer));
			cell.setStepFunction(getStepFunction());
			return cell;
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue().getValue().getData()));
		column.setMaxWidth(400);
		column.setMinWidth(100);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
		if (firstEditableColumn == null)
			firstEditableColumn = table.getColumns().size() -1;
	}

	public void addCheckboxColumn(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		TreeTableColumn<RecursiveWrapper<T>, Boolean> column = new TreeTableColumn<>(name);
		column.setCellValueFactory(param -> {
			return GenericBinding.of(getter, setter, param.getValue().getValue().getData());
		});
		column.setCellFactory(param -> new BooleanCell<>(column));
		column.setMinWidth(100);
		column.setEditable(false);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	public void addDeleteColumn(String name) {
		addDeleteColumn(name, null);
	}
	
	public void addDeleteColumn(String name, Consumer<T> listener) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory(c -> new CustomActionsCell());
		column.setMinWidth(100);
		column.setEditable(false);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);


		customActions.add(new CustomAction(FontAwesomeIcon.TIMES, (wrappedItem) -> {
			Platform.runLater( () -> {
				obsWrappedItems.remove(wrappedItem);
				if (listener != null) {
					listener.accept(wrappedItem.getData());
				}
			});
		}));
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
				obsWrappedItems.add(new RecursiveWrapper<>(newItem));
				return true;
			}
		}
		return false;
	}

	public void disableAddition() {
		addItemBtn.setVisible(false);
	}

	public void addCustomAction(FontAwesomeIcon icon, Consumer<T> action){
		customActions.add(new CustomAction(icon, wrappedItem -> action.accept(wrappedItem.getData())));
	}

	public void setItems(List<T> items) {
		setItems(items, null);
	}
	public void setItems(List<T> items, Comparator<T> comparator) {
		List<RecursiveWrapper<T>> wrappedItems = items.stream().map(i -> new RecursiveWrapper<>(i)).collect(Collectors.toList());
		obsWrappedItems = FXCollections.observableList(wrappedItems);
		if (comparator != null) {
			FXCollections.sort(obsWrappedItems, (ra, rb) -> comparator.compare(ra.getData(), rb.getData()));
		}
		
		
		obsWrappedItems.addListener(new ListChangeListener<RecursiveWrapper<T>>() {

			@Override
			public void onChanged(Change<? extends RecursiveWrapper<T>> c) {
				//forward removals:
				if (!c.next())
					return;
				
				if (c.wasRemoved()) {
					for(var ri : c.getRemoved()) {
						items.remove(ri.getData());
					}
				}
				
				if (c.wasAdded()) {
					RecursiveWrapper<T> newEntry = c.getAddedSubList().get(0);
					items.add(newEntry.getData());
				}
			}
		});
		
		TreeItem<RecursiveWrapper<T>> root = new RecursiveTreeItem<>(obsWrappedItems, RecursiveTreeObject::getChildren);
		table.setRoot(root);
		
		Platform.runLater(() -> {
			table.resizeColumns();
		});
		
		//register double-click listener for empty rows, to add a new instance
//		this.setRowFactory(view -> {
//		    TableRow<T> row = new TableRow<T>();
//		    row.setOnMouseClicked(event -> {
//			    if (row.isEmpty() && (event.getClickCount() == 2)) {
//			        T myItem = newItemCreator.get();
//			        getItems().add(myItem);
//			    } 
//			});
//		    return row;
//		});
		
		//we cant click on row if there is no row, so we have to register another event handler
		//for when the table is empty
//		this.setOnMouseClicked(event -> {
//		    if (getItems().isEmpty() && (event.getClickCount() == 2)) {
//		        T myItem = newItemCreator.get();
//		        getItems().add(myItem);
//		    } 
//		});
		
	}
	
	public void clearContent() {
		table.getColumns().clear();
	}



	private final class CustomActionsCell extends TreeTableCell<RecursiveWrapper<T>, String> {

		private JFXButton createButton(CustomAction action) {
			JFXButton btn = new JFXButton();
			btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			btn.setGraphic(new FontAwesomeIconView(action.getIcon(), "1.5em"));
			btn.setOnAction(event -> {
				RecursiveWrapper<T> wrappedItem = getTreeTableRow().getItem();
				action.getAction().accept(wrappedItem);
				getTreeTableView().refresh();
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




	public class BooleanCell<T2> extends JFXTreeTableCell<T2, Boolean> {
        private final CheckBox checkBox;
        
        public BooleanCell(TreeTableColumn<RecursiveWrapper<T>, ?> column) {
            checkBox = new CheckBox();
            
//            checkBox.setDisable(true);
            checkBox.setOnAction(e -> {
        			var row = getTreeTableRow().getIndex();
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
		Consumer<RecursiveWrapper<T>> action;
	}
	
}
