package milkman.utils.javafx;

import java.lang.reflect.Field;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import lombok.SneakyThrows;

public class SettableTreeItem<T> extends TreeItem<T> {

	
	
	
	@SneakyThrows
	public void setChildren(ObservableList<TreeItem<T>> children) {
		Field fc = TreeItem.class.getDeclaredField("children");
		fc.setAccessible(true);
		fc.set(this, children);
		
		Field f = TreeItem.class.getDeclaredField("childrenListener");
		f.setAccessible(true);
		ListChangeListener<TreeItem<T>> object = (ListChangeListener<TreeItem<T>>) f.get(this);
		
        children.addListener(object);

	}
}
