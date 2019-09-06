package milkmancli;

import milkman.domain.RequestAspect;

public interface AspectCliImportExport {

	boolean canHandleAspect(RequestAspect aspect);

	String exportToString(RequestAspect aspect);
	void applyNewValue(RequestAspect aspect, String value);

}
