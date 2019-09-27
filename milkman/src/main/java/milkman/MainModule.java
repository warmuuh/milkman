package milkman;

import java.util.Collections;
import java.util.stream.Collectors;

import milkman.ui.components.AutoCompleter;
import milkman.ui.main.ActiveEnvironmentProvider;
import wrm.hardwire.Module;

@Module
public class MainModule extends MainModuleBase {

	@Override
	protected AutoCompleter createAutoCompleter() {
		return new AutoCompleter(() -> {
			return getWorkspaceController().getActiveWorkspace().getEnvironments().stream()
					.filter(e -> e.isGlobal() || e.isActive())
					.flatMap(e -> e.getEntries().stream())
					.collect(Collectors.toList());
		});
	}

	@Override
	protected ActiveEnvironmentProvider createActiveEnvironmentProvider() {
		return new ActiveEnvironmentProvider(() -> getWorkspaceController());
	}

	
	
	
}
