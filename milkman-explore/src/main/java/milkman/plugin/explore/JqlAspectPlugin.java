package milkman.plugin.explore;

import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class JqlAspectPlugin implements RequestAspectsPlugin,AutoCompletionAware {

	private AutoCompleter completer;

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.emptyList();
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.singletonList(new JqlAspectResponseEditor(completer));
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof RestRequestContainer && !request.getAspect(JqlQueryAspect.class).isPresent())
			request.addAspect(new JqlQueryAspect());
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
	
	}

	@Override
	public int getOrder() {
		return 35;
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
		
	}

	
}
