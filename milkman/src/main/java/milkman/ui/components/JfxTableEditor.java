package milkman.ui.components;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.cells.editors.base.JFXTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.vavr.Function1;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.layout.StackPane;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import milkman.utils.fxml.GenericBinding;
import com.jfoenix.controls.JFXButton;

@Value
@EqualsAndHashCode(callSuper = true)
class RecursiveWrapper<T> extends RecursiveTreeObject<RecursiveWrapper<T>>{
	T data;
}

public class JfxTableEditor<T> extends StackPane {
	
	
	private JFXTreeTableView<RecursiveWrapper<T>> table = new JFXTreeTableView<RecursiveWrapper<T>>();

	private ObservableList<RecursiveWrapper<T>> obsWrappedItems;

	private JFXButton addItemBtn;


	public JfxTableEditor() {
		table.setShowRoot(false);
		this.getChildren().add(table);
		addItemBtn = new JFXButton();
		addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PLUS, "1.5em"));
		addItemBtn.getStyleClass().add("btn-add-entry");
		StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));
		this.getChildren().add(addItemBtn);
		
	}
	
	
	public void addColumn(String name, Function1<T, String> getter, BiConsumer<T, String> setter) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory((TreeTableColumn<RecursiveWrapper<T>, String> param) -> new GenericEditableTreeTableCell<RecursiveWrapper<T>, String>(new TextFieldEditorBuilder()));
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue().getValue().getData()));
		column.setMaxWidth(400);
		table.getColumns().add(column);
	}

	public void addCheckboxColumn(String name, Function1<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		TreeTableColumn<RecursiveWrapper<T>, Boolean> column = new TreeTableColumn<>(name);
		column.setCellValueFactory(param -> {
			return GenericBinding.of(getter, setter, param.getValue().getValue().getData());
		});
		column.setCellFactory(param -> new BooleanCell<>(column));
		table.getColumns().add(column);
	}

	public void addDeleteColumn(String name) {
		addDeleteColumn(name, null);
	}
	
	public void addDeleteColumn(String name, Runnable listener) {
		TreeTableColumn<RecursiveWrapper<T>, String> column = new TreeTableColumn<>(name);
		column.setCellFactory(c -> new DeleteEntryCell(listener));
		table.getColumns().add(column);
	}

	public void setEditable(boolean editable) {
		table.setEditable(editable);
		addItemBtn.setVisible(editable);
	}
	
	public void setItems(List<T> items, Supplier<T> newItemCreator) {
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
		
		this.addItemBtn.setOnAction(e -> { 
					obsWrappedItems.add(new RecursiveWrapper<>(newItemCreator.get()));
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
					});
//                        T element = getTreeTableView().getRoot().getChildren().get(getTreeTableRow().getIndex()).getValue();
//                        getItems().remove(element);
		        	if (listener != null)
		        		listener.run();
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
