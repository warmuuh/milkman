package milkman.plugin.note;

import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;

public class NotesAspectPlugin implements RequestAspectsPlugin {

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new NotesAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList();
	}

	@Override
	public void initializeAspects(RequestContainer request) {
		if (!request.hasAspect(NotesAspect.class))
			request.addAspect(new NotesAspect());
	}

	@Override
	public void initializeAspects(ResponseContainer response) {

	}

}
