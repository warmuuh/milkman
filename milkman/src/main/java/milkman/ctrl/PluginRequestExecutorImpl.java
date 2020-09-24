package milkman.ctrl;

import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.PluginRequestExecutor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.utils.AsyncResponseControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;


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
	public ResponseContainer executeRequest(RequestContainer requestContainer) {
		RequestTypePlugin requestTypePlugin = requestTypeManager.getPluginFor(requestContainer);
		var responseControl = new AsyncResponseControl();
		return requestTypePlugin.executeRequestAsync(requestContainer, workspaceController.buildTemplater(), responseControl.getCancellationControl());
	}

}
