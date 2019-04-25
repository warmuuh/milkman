package milkman.ui.plugin;

import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;

/**
* extension point for options-pages
*/
public interface OptionPageProvider<T extends OptionsObject> {

	/**
    * returns a POJO with all option-values. If setOptions was called before, this
    * method should return the latest instance, otherwise, it should return a new instance
    * with default-settings.
    */
	public T getOptions();

    /**
    * sets the current options. This is called on startup when the options are loaded.
    */
	public void setOptions(T options);

    /**
    * returns the ui for editing the options. For building the page, the provided builder can be used.
    */
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder);
	
	/**
	 * defines the order between aspect tabs, the higher, the more to the right
	 */
	public default int getOrder() {
		return Integer.MAX_VALUE;
	}
}
