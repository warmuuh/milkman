package milkman.ui.components;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.vavr.Function1;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import milkman.utils.fxml.GenericBinding;

/**
 * 
 * @author peter
 *
 * @deprecated use JfxTableEditor
 */
@Deprecated
public class TableEditor<T> extends TableView<T> {

	
//	private Supplier<T> newItemCreator;


	public void addColumn(String name, Function1<T, String> getter, BiConsumer<T, String> setter) {
		TableColumn<T, String> column = new TableColumn<>(name);
		column.setCellFactory(TextFieldTableCell.forTableColumn());
		column.setCellValueFactory(param -> GenericBinding.of(getter, setter, param.getValue()));
		this.getColumns().add(column);
	}

	public void addCheckboxColumn(String name, Function1<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		TableColumn<T, Boolean> column = new TableColumn<>(name);
		column.setCellFactory(c -> new BooleanCell<>());
		column.setCellValueFactory(param -> {
			return GenericBinding.of(getter, setter, param.getValue());
		});
		this.getColumns().add(column);
	}

	public void addDeleteColumn(String name) {
		TableColumn<T, String> column = new TableColumn<>(name);
		column.setCellFactory(c -> new TableCell<T, String>() {

            final Button btn = new Button("x");

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    btn.setOnAction(event -> {
                        T element = getTableView().getItems().get(getIndex());
                        getItems().remove(element);
                    });
                    setGraphic(btn);
                    setText(null);
                }
            }
        });
		this.getColumns().add(column);
	}

	
	public void setItems(List<T> items, Supplier<T> newItemCreator) {
		super.setItems(FXCollections.observableList(items));

		//register double-click listener for empty rows, to add a new instance
		this.setRowFactory(view -> {
		    TableRow<T> row = new TableRow<T>();
		    row.setOnMouseClicked(event -> {
			    if (row.isEmpty() && (event.getClickCount() == 2)) {
			        T myItem = newItemCreator.get();
			        getItems().add(myItem);
			    } 
			});
		    return row;
		});
		
		//we cant click on row if there is no row, so we have to register another event handler
		//for when the table is empty
		this.setOnMouseClicked(event -> {
		    if (getItems().isEmpty() && (event.getClickCount() == 2)) {
		        T myItem = newItemCreator.get();
		        getItems().add(myItem);
		    } 
		});
		
	}
	



	public class BooleanCell<T2> extends TableCell<T, Boolean> {
        private CheckBox checkBox;
        public BooleanCell() {
            checkBox = new CheckBox();
            
            
//            checkBox.setDisable(true);
            checkBox.setOnAction(e -> {
        			TableEditor.this.edit(getIndex(), getTableColumn());
//        			itemProperty().setValue(newValue == null ? false : newValue);
                	//if(isEditing())
                        commitEdit(!checkBox.isSelected());
            });
            this.setGraphic(checkBox);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            this.setEditable(true);
        }
//        @Override
//        public void startEdit() {
//            super.startEdit();
//            if (isEmpty()) {
//                return;
//            }
//            checkBox.setDisable(false);
//            checkBox.requestFocus();
//        }
//        @Override
//        public void cancelEdit() {
//            super.cancelEdit();
////            checkBox.setDisable(true);
//        }
//        public void commitEdit(Boolean value) {
//            super.commitEdit(value);
////            checkBox.setDisable(true);
//        }
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
