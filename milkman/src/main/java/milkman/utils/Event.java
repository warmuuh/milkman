package milkman.utils;

import io.vavr.CheckedConsumer;
import io.vavr.collection.List;
import lombok.SneakyThrows;

public class Event<T> {

	
	List<CheckedConsumer<T>> listeners = List.empty();
	

	
	public void add(CheckedConsumer<T> listener) {
		listeners = listeners.append(listener);
	}

	@SneakyThrows
	public void invoke(T payload) {
		for(var listener : listeners) {
			listener.accept(payload);
		}
	}
	
	
}
