package milkman.ui.plugin;

import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

public interface ExecutionListenerAware {

	void setExecutionListenerManager(ExecutionListenerManager manager);

	interface ExecutionListener {
		void onRequestStarted(RequestContainer request, ResponseContainer response);
		void onRequestReady(RequestContainer request, ResponseContainer response);
		void onRequestFinished(RequestContainer request, ResponseContainer response);
	}
}
