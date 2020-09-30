package milkman.ui.plugin;

import milkman.domain.Environment;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;

import java.util.Optional;

public interface PluginRequestExecutor {

	Optional<RequestContainer> getDetails(String requestId);

	ResponseContainer executeRequest(RequestContainer request, Optional<Environment> environmentOverride);
}
