package milkman.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import lombok.Data;
import lombok.SneakyThrows;

public class Event2<T1, T2> implements Observable {

	
	List<BiConsumer<T1, T2>> listeners = new ArrayList<>();
	

	
	public void add(BiConsumer<T1, T2> listener) {
		listeners.add(listener);
	}

	public void remove(BiConsumer<T1, T2> listener) {
		listeners.remove(listener);
	}

	
	@SneakyThrows
	public void invoke(T1 payload1, T2 payload2) {
		for(var listener : listeners) {
			listener.accept(payload1, payload2);
		}
	}
	
	
	@Data
	private class InvalidationListenerAdapter implements BiConsumer<T1, T2>{
		private final InvalidationListener listener;
		@Override
		public void accept(T1 t, T2 u) {
			listener.invalidated(Event2.this);
		}
	}

	@Override
	public void addListener(InvalidationListener listener) {
		add(new InvalidationListenerAdapter(listener));
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		remove(new InvalidationListenerAdapter(listener));
	}
	
}
