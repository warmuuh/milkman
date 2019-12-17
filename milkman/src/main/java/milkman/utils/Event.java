package milkman.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class Event<T> {

	
	List<CheckedConsumer<T>> listeners = new ArrayList<CheckedConsumer<T>>();
	

	
	public void add(CheckedConsumer<T> listener) {
		listeners.add(listener);
	}

	@SneakyThrows
	public void invoke(T payload) {
		for(var listener : listeners) {
			listener.apply(payload);
		}
	}
	
	public void clear() {
		listeners.clear();
	}
	
	
}
