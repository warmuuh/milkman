package milkman.utils;


import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class Event0 {

	
	List<Runnable> listeners = new ArrayList<>();
	

	
	public void add(Runnable listener) {
		listeners.add(listener);
	}

	@SneakyThrows
	public void invoke() {
		for(var listener : listeners) {
			listener.run();
		}
	}
	
	public void clear() {
		listeners.clear();
	}
	
	
}
