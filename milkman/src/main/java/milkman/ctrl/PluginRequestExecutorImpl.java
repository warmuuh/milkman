package milkman.ctrl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.PluginRequestExecutor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;
import milkman.utils.AsyncResponseControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class PluginRequestExecutorImpl implements PluginRequestExecutor {

	private final RequestTypeManager requestTypeManager;
	private final WorkspaceController workspaceController;
	private final UiPluginManager plugins;

	@Override
	public Optional<RequestContainer> getDetails(String requestId) {
		return workspaceController.findRequestById(requestId);
	}

	@Override
	@SneakyThrows
	public ResponseContainer executeRequest(RequestContainer requestContainer, Optional<Environment> environmentOverride) {
		RequestTypePlugin requestTypePlugin = requestTypeManager.getPluginFor(requestContainer);
		RequestExecutionContext context = getExecutionCtx(environmentOverride);

		plugins.loadRequestAspectPlugins().forEach(a -> a.beforeRequestExecution(requestContainer, context));

		var responseControl = new AsyncResponseControl();
		var templater = environmentOverride.map(workspaceController::buildTemplater)
				.orElseGet(workspaceController::buildTemplater);
		var responseContainer = requestTypePlugin.executeRequestAsync(requestContainer, templater, responseControl.getCancellationControl());
		try{
			throw responseControl.onRequestFailed.get();
		} catch (InterruptedException|ExecutionException|CancellationException e) {
			/* fail-future got cancelled, everything ok */
		}

		plugins.loadRequestAspectPlugins().forEach(a -> a.initializeResponseAspects(requestContainer, responseContainer, context));

		return responseContainer;
	}

	private RequestExecutionContext getExecutionCtx(Optional<Environment> environmentOverride) {
		var activeEnv = workspaceController.getActiveWorkspace().getEnvironments().stream().filter(e -> e.isActive()).findAny();
		var effectiveActiveEnv = environmentOverride.or(() -> activeEnv);
		var globalEnvs = new LinkedList<>(workspaceController.getActiveWorkspace().getEnvironments().stream().filter(e -> e.isGlobal()).collect(Collectors.toList()));
		if (environmentOverride.isPresent()){ //add active as first global if override is active
			activeEnv.ifPresent(ae -> globalEnvs.add(0, ae));
		}

		return new RequestExecutionContext(effectiveActiveEnv, globalEnvs);
	}

}
