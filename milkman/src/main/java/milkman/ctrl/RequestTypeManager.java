package milkman.ctrl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.ui.main.dialogs.SelectValueDialog;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class RequestTypeManager {

	private final UiPluginManager plugins;
	
	
	
	public RequestContainer createNewRequest(boolean useDefault) {
		RequestTypePlugin requestTypePlugin = getRequestTypePlugin(useDefault);
		RequestContainer request = requestTypePlugin.createNewRequest();
		request.setId(UUID.randomUUID().toString());
		plugins.loadRequestAspectPlugins().forEach(p -> p.initializeRequestAspects(request));
		return request;
	}



	private RequestTypePlugin getRequestTypePlugin(boolean useDefault) {
		List<RequestTypePlugin> requestTypePlugins = plugins.loadRequestTypePlugins();
		
		if (requestTypePlugins.size() == 0)
			throw new IllegalArgumentException("No RequestType plugins found");
		
		if (requestTypePlugins.size() == 1 || useDefault)
			return requestTypePlugins.get(0);

		SelectValueDialog dialog = new SelectValueDialog();
		List<String> reqTypeNames = requestTypePlugins.stream().map(p -> p.getRequestType()).collect(Collectors.toList());
		dialog.showAndWait("New Request", "Select Request Type", Optional.of(reqTypeNames.get(0)), reqTypeNames);
//		if (!dialog.isCancelled()) {
			return requestTypePlugins.stream().filter(p -> p.getRequestType().equals(dialog.getInput())).findAny().get();
//		}
		
		
	}



	public RequestTypePlugin getPluginFor(RequestContainer request) {
		return  plugins.loadRequestTypePlugins().stream()
				.filter(p -> p.canHandle(request))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("no plugin can handle request of type " + request.getType()));
	}



	public RequestTypeEditor getRequestEditor(RequestContainer request) {
		return getPluginFor(request).getRequestEditor();
	}
	
	
	
	
}
