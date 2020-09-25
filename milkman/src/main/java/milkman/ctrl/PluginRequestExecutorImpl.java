package milkman.ctrl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.PluginRequestExecutor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.utils.AsyncResponseControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class PluginRequestExecutorImpl implements PluginRequestExecutor {

	private final RequestTypeManager requestTypeManager;
	private final WorkspaceController workspaceController;

	@Override
	public Optional<RequestContainer> getDetails(String requestId) {
		return workspaceController.findRequestById(requestId);
	}

	@Override
	@SneakyThrows
	public ResponseContainer executeRequest(RequestContainer requestContainer) {
		RequestTypePlugin requestTypePlugin = requestTypeManager.getPluginFor(requestContainer);
		var responseControl = new AsyncResponseControl();
		var responseContainer = requestTypePlugin.executeRequestAsync(requestContainer, workspaceController.buildTemplater(), responseControl.getCancellationControl());
		try{
			throw responseControl.onRequestFailed.get();
		} catch (InterruptedException|ExecutionException|CancellationException e) {
			/* fail-future got cancelled, everything ok */
		}
		return responseContainer;
	}

}
