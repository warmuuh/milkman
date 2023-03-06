package milkman.ctrl;

import java.util.LinkedList;
import java.util.List;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.ExecutionListenerAware.ExecutionListener;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ExecutionListenerManager {
	private final Map<String, Map<String, ExecutionListener>> listeners = new HashMap<>();
	private final List<ExecutionListener> generalListeners = new LinkedList<>();

	public void listenOnExecution(ExecutionListener listener) {
		generalListeners.add(listener);
	}

	public void listenOnExecution(RequestContainer request, String listenerId, ExecutionListener listener) {
		var requestListeners = listeners.computeIfAbsent(request.getId(), id -> new HashMap<>());
		requestListeners.put(listenerId, listener);
	}

	/* package */ void onRequestStarted(RequestContainer request, ResponseContainer response) {
		generalListeners.forEach(l ->  l.onRequestStarted(request, response));

		Optional.ofNullable(listeners.get(request.getId()))
				.map(Map::values)
				.ifPresent(listeners ->
						listeners.forEach(l -> l.onRequestStarted(request, response)));
	}

	/* package */ void onRequestReady(RequestContainer request, ResponseContainer response) {
		generalListeners.forEach(l ->  l.onRequestReady(request, response));

		Optional.ofNullable(listeners.get(request.getId()))
				.map(Map::values)
				.ifPresent(listeners ->
						listeners.forEach(l -> l.onRequestReady(request, response)));
	}

	/* package */ void onRequestFinished(RequestContainer request, ResponseContainer response) {
		generalListeners.forEach(l ->  l.onRequestFinished(request, response));

		Optional.ofNullable(listeners.get(request.getId()))
				.map(Map::values)
				.ifPresent(listeners ->
						listeners.forEach(l -> l.onRequestFinished(request, response)));
	}

	/* package */ void clearListeners(RequestContainer request) {
		listeners.remove(request.getId());
	}

}
