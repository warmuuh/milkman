package milkmancli;

import milkman.domain.RequestAspect;
import milkman.domain.ResponseAspect;
import milkman.ui.plugin.Orderable;

public interface AspectCliPresenter extends Orderable {

	/**
	 * states, if the aspect should be shown on normal display or only on verbose display
	 */
	boolean isPrimary();
	
	
	boolean canHandleAspect(ResponseAspect aspect);

	String getStringRepresentation(ResponseAspect aspect);
	
}
