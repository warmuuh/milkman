package milkman.utils;

import java.util.function.BiConsumer;

import io.vavr.CheckedConsumer;
import io.vavr.collection.List;
import lombok.SneakyThrows;
import lombok.var;

public class Event2<T1, T2> {

	
	List<BiConsumer<T1, T2>> listeners = List.empty();
	

	
	public void add(BiConsumer<T1, T2> listener) {
		listeners = listeners.append(listener);
	}

	@SneakyThrows
	public void invoke(T1 payload1, T2 payload2) {
		for(var listener : listeners) {
			listener.accept(payload1, payload2);
		}
	}
	
	
}
