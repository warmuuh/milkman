package milkman.utils.javafx;

import java.lang.reflect.Method;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.skin.TableViewSkinBase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResizableJfxTreeTableView<R extends RecursiveTreeObject<R>> extends JFXTreeTableView<R> {

	public ResizableJfxTreeTableView() {
		setSkin(createDefaultSkin());
	}
//	@Override
//	protected Skin<?> createDefaultSkin() {
//		return new ResizableJfxTreeTableViewSkin<>(this);
//	}

	public void resizeColumns() {
		try {
			Method method = getClass().getClassLoader()
						.loadClass("javafx.scene.control.skin.TableSkinUtils")
						.getMethod("resizeColumnToFitContent", TableViewSkinBase.class, TableColumnBase.class, int.class);
			method.setAccessible(true);
	
			for (TreeTableColumn<R, ?> column : this.getColumns()) {
				method.invoke(null,  getSkin(), column, -1);
			}
		} catch (Throwable t) {
			log.warn("Failed to resize columns", t);
		}
//		TableSkinUtils.resizeColumnToFitContent((TableViewSkinBase<?, ?, ?, ?, ?>) getSkin(), getTreeColumn(), -1);
//		((ResizableJfxTreeTableViewSkin<R>)getSkin()).resizeAllColumns();
	}
	
}
