package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.SyncDetails;
import milkman.domain.Workspace;

/**
* extension point for workspace synchronization methods.
*/
public interface WorkspaceSynchronizer extends Orderable {

	/**
    * checks, if the workspaces is setup for synchronization with this plugin.
    */
	boolean supportSyncOf(Workspace workspace);

    /**
    * triggers a synchronization, blocking, will be executed in a javafx-service
    */
	void synchronize(Workspace workspace, boolean syncLocalOnly);


    /**
    * returns a detail-factory that provides the UI for setting up a workspace synchronization
    */
	public SynchronizationDetailFactory getDetailFactory();

    /**
    * creates implementation-specific SyncDetails and the UI to edit them
    */
	public interface SynchronizationDetailFactory {
        /**
        * the name of the synchronization method. Will be shown in ui
        */
		String getName();

        /**
        * Root-node of the UI to edit sync details
        */
        Node getSyncDetailsControls();

        /**
        * creates the sync details after user entered all necessary data
        */
		SyncDetails createSyncDetails();
	}

}
