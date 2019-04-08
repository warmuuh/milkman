package milkman.ui.plugin;

import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;

public interface OptionPageProvider<T extends OptionsObject> {

	
	public T getOptions();
	public void setOptions(T options);

	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder);
	
}
