package milkman.ui.plugin;

import java.util.List;
import javafx.scene.Node;
import lombok.Value;
import milkman.domain.Collection;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;

/**
* extension point for libraries
*/
public interface LibraryPlugin {

    /**
    * name of the library type, will show up in UI
    */
	public String getName();


	public String getDefaultUrl();

    /**
    * returns a list of entries that match the searchInput
    */
	public List<LibraryEntry> lookupEntry(String url, String searchInput);

	/**
	 * triggers the import
	 */
	public Collection importCollection(String url, String libraryEntryId);

	@Value
	static class LibraryEntry {
		String displayName;
		String id;
	}
}
