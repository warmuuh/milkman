package milkman.ctrl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

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

	public CompletableFuture<Void> syncWorkspace(Workspace workspace, Toaster toaster) {
		if (!workspace.getSyncDetails().isSyncActive()) {
			CompletableFuture<Void> future = new CompletableFuture<Void>();
			future.completeExceptionally(new RuntimeException("Sync not active"));
			return future;
		}
		
		for (WorkspaceSynchronizer synchronizer : plugins.loadSyncPlugins()) {
			if (synchronizer.supportSyncOf(workspace)) {
				return triggerSynchronization(workspace, synchronizer, toaster);
			}
		}
		
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		future.completeExceptionally(new RuntimeException("No matching sync plugin found"));
		return future;
	}

	
	private CompletableFuture<Void> triggerSynchronization(Workspace workspace, WorkspaceSynchronizer synchronizer, Toaster toaster) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		SyncServiceTask task = new SyncServiceTask(workspace, synchronizer);
		
		task.setOnSucceeded(e -> {
			SyncResult result = task.getValue();
			if (result.isSuccess()) {
				future.complete(null);
			} else {
				future.completeExceptionally(result.getT());
			}
		});
		
		task.setOnFailed(e -> {
			future.completeExceptionally(new RuntimeException("Sync Failed"));
		});
		
		task.start();
		
		return future;
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
