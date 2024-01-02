package milkman.ui.main.options;

@FunctionalInterface
public interface VetoableListItemRemovalListener<T> {
	boolean isElementRemovalAllowed(T element);
}
