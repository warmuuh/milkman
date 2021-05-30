package milkman;

import milkman.ui.components.AutoCompleter;
import milkman.ui.main.ActiveEnvironmentProvider;
import milkman.ui.plugin.UiPluginManager;
import wrm.hardwire.Module;

import java.util.stream.Collectors;

@Module
public class MainModule extends MainModuleBase {

//	@Override
	protected AutoCompleter createAutoCompleter() {
		return new AutoCompleter(() -> {
			return getWorkspaceController().getActiveWorkspace().getEnvironments().stream()
					.filter(e -> e.isGlobal() || e.isActive())
					.flatMap(e -> e.getEntries().stream())
					.collect(Collectors.toList());
		}, () -> getUiPluginManager().loadTemplaterPlugins());
	}

//	@Override
	protected ActiveEnvironmentProvider createActiveEnvironmentProvider() {
		return new ActiveEnvironmentProvider(() -> getWorkspaceController());
	}

	@Override
	protected UiPluginManager createUiPluginManager() {
		return new UiPluginManager(createAutoCompleter(),
				createActiveEnvironmentProvider(),
				() -> getToaster(),
				() -> getPluginRequestExecutorImpl(),
				() -> getWorkspaceController().getActiveWorkspace(),
				getExecutionListenerManager()
				);
	}

	
	
	
}
