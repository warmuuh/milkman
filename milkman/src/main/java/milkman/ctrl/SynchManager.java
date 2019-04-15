package milkman.ctrl;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkman.domain.Workspace;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.WorkspaceSynchronizer;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class SynchManager {

	private final UiPluginManager plugins;

	public void syncWorkspace(Workspace workspace) {
		if (!workspace.getSyncDetails().isSyncActive())
			return;
		
		for (WorkspaceSynchronizer synchronizer : plugins.loadSyncPlugins()) {
			if (synchronizer.supportSyncOf(workspace)) {
				synchronizer.synchronize(workspace);
				return;
			}
		}
	}

}
