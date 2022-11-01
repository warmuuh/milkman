package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.RequestContainer;
import milkman.ui.main.Toaster;

public interface Exporter<T> extends Orderable {

	/**
	 * returns the pretty name of the exporter, shows up in UI
	 */
	public String getName();
	

	/**
	 * returns the root of the UI-controls for setting up the export details 
	 */
	public Node getRoot(T request, Templater templater);
	
	
	/**
	 * returns true if the exported data is already visible in the UI-Controls, so no extra "export"-button is necessary 
	 */
	public boolean isAdhocExporter();
	
	/**
	 * 
	 * returns true, if the dialog should be closed (e.g. if the operation was success, but not necessarily, maybe some details should be shown after export)
	 */
	public boolean doExport(T collection, Templater templater, Toaster toaster);

	@Override
	default int getOrder() {
		return 500;
	}
}
