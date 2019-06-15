package milkman.ui.components;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.cells.editors.base.JFXTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.vavr.Function1;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.EqualsAndHashCode;
import lombok.Value;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.javafx.ResizableJfxTreeTableView;

@Value
@EqualsAndHashCode(callSuper = true)
class RecursiveWrapper<T> extends RecursiveTreeObject<RecursiveWrapper<T>>{
	T data;
}

public class JfxTableEditor<T> extends StackPane {
	
	
	private ResizableJfxTreeTableView<RecursiveWrapper<T>> table = new ResizableJfxTreeTableView<RecursiveWrapper<T>>();

	private ObservableList<RecursiveWrapper<T>> obsWrappedItems;

	private JFXButton addItemBtn;

	
	private Function<T, String> rowToStringConverter = null;
	
	private static class SelectableTextFieldBuilder extends TextFieldEditorBuilder {

		@Override
		public Region createNode(String value, EventHandler<KeyEvent> keyEventsHandler,
				ChangeListener<Boolean> focusChangeListener) {
			Region node = super.createNode(value, keyEventsHandler, focusChangeListener);
			
			this.textField.setEditable(false);
			
			return node;
		}
		
		
	}

	public JfxTableEditor() {
		table.setShowRoot(false);
		table.setEditable(true);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.getChildren().add(table);
		addItemBtn = new JFXButton();
		addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLUS, "1.5em"));
		addItemBtn.getStyleClass().add("btn-add-entry");
		StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));
		this.getChildren().add(addItemBtn);
		
		final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
	    table.setOnKeyPressed(event -> {
	        if (keyCodeCopy.match(event)) {
	            copySelectionToClipboard();
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
		
		final ClipboardContent clipboardContent = new ClipboardContent();
	    clipboardContent.putString(b.toString());
	    Clipboard.getSystemClipboard().setContent(clipboardContent);
	}

	/**
	 * used for converting selected rows to clipboard content
	 * 
	 * @param rowToStringConverter
	 */
	public void setRowToStringConverter(Function<T, String> rowToStringConverter) {
		this.rowToStringConverter = rowToStringConverter;
	}
	

	public void addReadOnlyColumn(String name, Function1<T, String> getter) {
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
	
	public void addColumn(String name, Function1<T, String> getter, BiConsumer<T, String> setter) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory((TreeTableColumn<RecursiveWrapper<T>, String> param) -> {
			return new GenericEditableTreeTableCell<RecursiveWrapper<T>, String>(new TextFieldEditorBuilder());
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue().getValue().getData()));
		column.setMaxWidth(400);
		column.setMinWidth(100);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}
	
	public void addColumn(String name, Function1<T, String> getter, BiConsumer<T, String> setter, Consumer<TextField> textFieldInitializer) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory((TreeTableColumn<RecursiveWrapper<T>, String> param) -> {
			return new GenericEditableTreeTableCell<RecursiveWrapper<T>, String>(new InitializingCellBuilder(textFieldInitializer));
		});
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue().getValue().getData()));
		column.setMaxWidth(400);
		column.setMinWidth(100);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	public void addCheckboxColumn(String name, Function1<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		TreeTableColumn<RecursiveWrapper<T>, Boolean> column = new TreeTableColumn<>(name);
		column.setCellValueFactory(param -> {
			return GenericBinding.of(getter, setter, param.getValue().getValue().getData());
		});
		column.setCellFactory(param -> new BooleanCell<>(column));
		column.setMinWidth(100);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	public void addDeleteColumn(String name) {
		addDeleteColumn(name, null);
	}
	
	public void addDeleteColumn(String name, Runnable listener) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory(c -> new DeleteEntryCell(listener));
		column.setMinWidth(100);
		table.getColumns().add(column);
//		column.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	public void enableAddition(Supplier<T> newItemCreator) {
		addItemBtn.setVisible(true);
		this.addItemBtn.setOnAction(e -> { 
			obsWrappedItems.add(new RecursiveWrapper<>(newItemCreator.get()));
		});
	}
	public void disableAddition() {
		addItemBtn.setVisible(false);
	}
	
	
	public void setItems(List<T> items) {
		List<RecursiveWrapper<T>> wrappedItems = items.stream().map(i -> new RecursiveWrapper<>(i)).collect(Collectors.toList());
		obsWrappedItems = FXCollections.observableList(wrappedItems);
		obsWrappedItems.addListener(new ListChangeListener<RecursiveWrapper<T>>() {

			@Override
			public void onChanged(Change<? extends RecursiveWrapper<T>> c) {
				//forward removals:
				if (!c.next())
					return;
				
				if (c.wasRemoved()) {
					items.remove(c.getFrom());
				}
				
				if (c.wasAdded()) {
					RecursiveWrapper<T> newEntry = c.getAddedSubList().get(0);
					items.add(newEntry.getData());
				}
			}
		});
		
		final TreeItem<RecursiveWrapper<T>> root = new RecursiveTreeItem<>(obsWrappedItems, RecursiveTreeObject::getChildren); 
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



	private final class DeleteEntryCell extends TreeTableCell<RecursiveWrapper<T>, String> {
		final JFXButton btn;
		private Runnable listener;
		
		public DeleteEntryCell(Runnable listener) {
			this.listener = listener;
			btn = new JFXButton();
			btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			btn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "1.5em"));
		}
		
		@Override
		public void updateItem(String item, boolean empty) {
		    super.updateItem(item, empty);
		    if (empty) {
		        setGraphic(null);
		        setText(null);
		    } else {
		        btn.setOnAction(event -> {

					Platform.runLater( () -> {
//						table.getRoot().getChildren().remove(getTreeTableRow().getIndex());
						obsWrappedItems.remove(getTreeTableRow().getIndex());
//						table.getRoot().getValue().setChildren(obsWrappedItems);
//						table.setRoot(table.getRoot());
//						table.refresh();
						if (listener != null)
			        		listener.run();
					});
//                        T element = getTreeTableView().getRoot().getChildren().get(getTreeTableRow().getIndex()).getValue();
//                        getItems().remove(element);
		        	
		        });
		        setGraphic(btn);
		        setText(null);
		    }
		}
	}




	public class BooleanCell<T2> extends JFXTreeTableCell<T2, Boolean> {
        private CheckBox checkBox;
        
        public BooleanCell(TreeTableColumn<RecursiveWrapper<T>, ?> column) {
            checkBox = new CheckBox();
            
            
//            checkBox.setDisable(true);
            checkBox.setOnAction(e -> {
        			table.edit(BooleanCell.this.getTreeTableRow().getIndex(), column);
//        			itemProperty().setValue(newValue == null ? false : newValue);
                	//if(isEditing())
                        commitEdit(!checkBox.isSelected());
            });
            this.setGraphic(checkBox);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            this.setEditable(true);
        }

        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                this.setGraphic(null);
            } else {
                checkBox.setSelected(item);
            	this.setGraphic(checkBox);
            }
        }
    }
}
