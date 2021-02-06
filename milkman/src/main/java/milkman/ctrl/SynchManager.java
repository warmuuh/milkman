package milkman.ctrl;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Workspace;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.WorkspaceSynchronizer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Slf4j
public class SynchManager {

	private final UiPluginManager plugins;

	public CompletableFuture<Void> syncWorkspace(Workspace workspace, boolean localSyncOnly) {
		if (!workspace.getSyncDetails().isSyncActive()) {
			CompletableFuture<Void> future = new CompletableFuture<Void>();
			future.completeExceptionally(new RuntimeException("Sync not active"));
			return future;
		}
		
		for (WorkspaceSynchronizer synchronizer : plugins.loadSyncPlugins()) {
			if (synchronizer.supportSyncOf(workspace)) {
				return triggerSynchronization(workspace, synchronizer, localSyncOnly);
			}
		}
		
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		future.completeExceptionally(new RuntimeException("No matching sync plugin found"));
		return future;
	}


	private CompletableFuture<Void> triggerSynchronization(Workspace workspace,
														   WorkspaceSynchronizer synchronizer,
														   boolean localSyncOnly) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		SyncServiceTask task = new SyncServiceTask(workspace, synchronizer, localSyncOnly);

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
		private final boolean localSyncOnly;
		
		@Override
		protected Task<SyncResult> createTask() {
			return new Task<SyncResult>() {

				@Override
				protected SyncResult call() throws Exception {
					try {
						synchronizer.synchronize(workspace, localSyncOnly);
						return new SyncResult(true, null);
					} catch (Throwable t) {
						log.warn("Failed to sync", t);
						return new SyncResult(false, t);
					}
				}};
		}
		
	}
	
}
