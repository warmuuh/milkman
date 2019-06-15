package milkman.utils.javafx;

import java.lang.reflect.Method;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.TableViewSkinBase;
import lombok.SneakyThrows;

public class ResizableJfxTreeTableView<R extends RecursiveTreeObject<R>> extends JFXTreeTableView<R> {

	public ResizableJfxTreeTableView() {
		setSkin(createDefaultSkin());
	}
//	@Override
//	protected Skin<?> createDefaultSkin() {
//		return new ResizableJfxTreeTableViewSkin<>(this);
//	}

	@SneakyThrows
	public void resizeColumns() {
		Method method = getClass().getClassLoader()
					.loadClass("javafx.scene.control.skin.TableSkinUtils")
					.getMethod("resizeColumnToFitContent", TableViewSkinBase.class, TableColumnBase.class, int.class);
		method.setAccessible(true);
		method.invoke(null,  getSkin(), getTreeColumn(), -1);
//		TableSkinUtils.resizeColumnToFitContent((TableViewSkinBase<?, ?, ?, ?, ?>) getSkin(), getTreeColumn(), -1);
//		((ResizableJfxTreeTableViewSkin<R>)getSkin()).resizeAllColumns();
	}
	
}
