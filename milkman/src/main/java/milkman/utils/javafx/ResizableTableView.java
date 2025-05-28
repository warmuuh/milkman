package milkman.utils.javafx;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import java.lang.reflect.Method;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResizableTableView<R extends RecursiveTreeObject<R>> extends TableView<R> {

	public ResizableTableView() {
		setSkin(createDefaultSkin());
	}
//	@Override
//	protected Skin<?> createDefaultSkin() {
//		return new ResizableJfxTreeTableViewSkin<>(this);
//	}

	@SneakyThrows
	public void resizeColumns() {
		TableViewSkin<?> skin = (TableViewSkin<?>) getSkin();

		Method getTableHeaderRow = TableViewSkinBase.class.getDeclaredMethod("getTableHeaderRow");
		getTableHeaderRow.setAccessible(true);
		TableHeaderRow headerRow = (TableHeaderRow) getTableHeaderRow.invoke(skin);
		NestedTableColumnHeader rootHeader = headerRow.getRootHeader();
		for (TableColumnHeader columnHeader : rootHeader.getColumnHeaders()) {
			try {
				TableColumn<?, ?> column = (TableColumn<?, ?>) columnHeader.getTableColumn();
				if (column != null) {
					Method method = TableColumnHeader.class.getDeclaredMethod("resizeColumnToFitContent", int.class);
					method.setAccessible(true);
					method.invoke(columnHeader, 30);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
}
