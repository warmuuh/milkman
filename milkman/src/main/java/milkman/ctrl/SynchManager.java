package milkman.ctrl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.WorkspaceSynchronizer;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Slf4j
public class SynchManager {

	private final UiPluginManager plugins;

	public void syncWorkspace(Workspace workspace, Toaster toaster) {
		if (!workspace.getSyncDetails().isSyncActive())
			return;
		
		for (WorkspaceSynchronizer synchronizer : plugins.loadSyncPlugins()) {
			if (synchronizer.supportSyncOf(workspace)) {
				try {
					synchronizer.synchronize(workspace);
				} catch (Throwable t) {
					log.warn("Failed to sync", t);
					toaster.showToast(ExceptionUtils.getRootCauseMessage(t));
				}
				return;
			}
		}
	}

}
