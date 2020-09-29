package milkmancli;

import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.PluginRequestExecutor;

import java.util.Optional;

public class CliPluginRequestExecutorImpl implements PluginRequestExecutor {
	@Override
	public Optional<RequestContainer> getDetails(String requestId) {
		return Optional.empty();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Optional<Environment> environmentOverride) {
		return null;
	}
}
