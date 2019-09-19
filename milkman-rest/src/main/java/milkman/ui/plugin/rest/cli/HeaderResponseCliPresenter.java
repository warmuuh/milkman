package milkman.ui.plugin.rest.cli;

import milkman.domain.ResponseAspect;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkmancli.AspectCliPresenter;

public class HeaderResponseCliPresenter implements AspectCliPresenter {

	@Override
	public boolean isPrimary() {
		return false;
	}

	@Override
	public boolean canHandleAspect(ResponseAspect aspect) {
		return aspect instanceof RestResponseHeaderAspect;
	}

	@Override
	public String getStringRepresentation(ResponseAspect aspect) {
		RestResponseHeaderAspect headers = (RestResponseHeaderAspect) aspect;
		StringBuilder b = new StringBuilder();
		headers.getEntries()
				.forEach(h -> b.append(h.getName()).append(": ").append(h.getValue()).append(System.lineSeparator()));
		return b.toString();
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
