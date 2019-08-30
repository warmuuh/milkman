package milkman.ui.plugin.rest.cli;

import milkman.domain.ResponseAspect;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkmancli.AspectCliPresenter;

public class BodyResponseCliPresenter implements AspectCliPresenter {

	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseBodyAspect;
	}

	@Override
	public String getStringRepresentation(ResponseAspect aspect) {
		RestResponseBodyAspect body = (RestResponseBodyAspect) aspect;
		return body.getBody();
	}

}
