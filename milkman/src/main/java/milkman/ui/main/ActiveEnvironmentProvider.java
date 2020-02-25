package milkman.ui.main;

import lombok.RequiredArgsConstructor;
import milkman.ctrl.WorkspaceController;
import milkman.domain.Environment;

import java.util.Optional;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class ActiveEnvironmentProvider {
	
	private final Supplier<WorkspaceController> wsCtrl;
	
	public Optional<Environment> getActiveEnvironment() {
		return wsCtrl.get().getActiveWorkspace().getEnvironments().stream()
				.filter(e -> e.isActive())
				.findAny();
	}
}
