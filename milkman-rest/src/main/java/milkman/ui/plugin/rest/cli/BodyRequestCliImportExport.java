package milkman.ui.plugin.rest.cli;

import milkman.domain.RequestAspect;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkmancli.AspectCliImportExport;

public class BodyRequestCliImportExport implements AspectCliImportExport {

	@Override
	public boolean canHandleAspect(RequestAspect aspect) {
		return aspect instanceof RestBodyAspect;
	}

	@Override
	public String exportToString(RequestAspect aspect) {
		return ((RestBodyAspect)aspect).getBody();
	}

	@Override
	public void applyNewValue(RequestAspect aspect, String value) {
		((RestBodyAspect)aspect).setBody(value);
	}

}
