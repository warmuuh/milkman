package milkman.utils;


import io.vavr.collection.List;
import lombok.SneakyThrows;

public class Event0 {

	
	List<Runnable> listeners = List.empty();
	

	
	public void add(Runnable listener) {
		listeners = listeners.append(listener);
	}

	@SneakyThrows
	public void invoke() {
		for(var listener : listeners) {
			listener.run();
		}
	}
	
	public void clear() {
		listeners = List.empty();
	}
	
	
}
