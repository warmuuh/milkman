package milkman.plugin.note;

import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
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
	public void initializeRequestAspects(RequestContainer request) {
		if (!request.getAspect(NotesAspect.class).isPresent())
			request.addAspect(new NotesAspect());
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {

	}

	@Override
	public int getOrder() {
		return 99;
	}

	
}
