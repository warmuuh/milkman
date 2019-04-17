package milkman.ctrl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.Value;
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
				triggerSynchronization(workspace, synchronizer, toaster);
				return;
			}
		}
	}

	
	private void triggerSynchronization(Workspace workspace, WorkspaceSynchronizer synchronizer, Toaster toaster) {
		SyncServiceTask task = new SyncServiceTask(workspace, synchronizer);
		
		task.setOnSucceeded(e -> {
			SyncResult result = task.getValue();
			if (result.isSuccess()) {
				toaster.showToast("Synchronized");
			} else {
				toaster.showToast("Sync failed: " + ExceptionUtils.getRootCauseMessage(result.getT()));
			}
		});
		
		task.setOnFailed(e -> {
			toaster.showToast("Sync Failed");
		});
		
		task.start();
	}


	@Value
	public static class SyncResult {
		boolean success;
		Throwable t;
	}
	
	
	@RequiredArgsConstructor
	private static class SyncServiceTask extends Service<SyncResult> {

		private final Workspace workspace;
		private final WorkspaceSynchronizer synchronizer;
		
		@Override
		protected Task<SyncResult> createTask() {
			return new Task<SyncResult>() {

				@Override
				protected SyncResult call() throws Exception {
					try {
						synchronizer.synchronize(workspace);
						return new SyncResult(true, null);
					} catch (Throwable t) {
						log.warn("Failed to sync", t);
						return new SyncResult(false, t);
					}
				}};
		}
		
	}
	
}
