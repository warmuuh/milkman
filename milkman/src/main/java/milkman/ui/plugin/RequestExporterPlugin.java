package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.RequestContainer;
import milkman.ui.main.Toaster;

/**
 * extension point for adding request exporters
 * 
 * @author peter
 *
 */
public interface RequestExporterPlugin {

	
	/**
	 * returns the pretty name of the exporter, shows up in UI
	 */
	public String getName();
	
	/**
	 * returns true if the supplied type of request can be exported by this plugin 
	 */
	public boolean canHandle(RequestContainer request);
	
	/**
	 * returns the root of the UI-controls for setting up the export details 
	 */
	public Node getRoot(RequestContainer request);
	
	
	/**
	 * returns true if the exported data is already visible in the UI-Controls, so no extra "export"-button is necessary 
	 */
	public boolean isAdhocExporter();
	
	
	/**
	 * in case of no adhoc-export, this will be called to issue the export operation
	 * 
	 * returns true, if the dialog should be closed (e.g. if the operation was success, but not necessarily, maybe some details should be shown after export)
	 */
	public boolean doExport(RequestContainer request, Toaster toaster);
	
	
}
