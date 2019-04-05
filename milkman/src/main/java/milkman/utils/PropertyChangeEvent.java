package milkman.utils;

public class PropertyChangeEvent<T> extends Event2<T, T> {

	@Override
	public void invoke(T oldValue, T newValue) {
		super.invoke(oldValue, newValue);
	}
	
}
