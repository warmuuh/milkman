package milkman.ctrl;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.main.dialogs.SelectValueDialog;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.UiPluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class RequestTypeManager {

	private final UiPluginManager plugins;
	
	
	
	public Optional<RequestContainer> createNewRequest(boolean useDefault) {
		Optional<RequestTypePlugin> requestTypePlugin = getRequestTypePlugin(useDefault) ;
		return requestTypePlugin.map(rType -> {
			RequestContainer request = rType.createNewRequest();
			request.setId(UUID.randomUUID().toString());
			plugins.loadRequestAspectPlugins().forEach(p -> p.initializeRequestAspects(request));
			return request;
		});
	}

	
	
	public CompletableFuture<Optional<RequestContainer>> createNewRequestQuick(Node quickSelectionNode) {
		var requestTypePluginF = getRequestTypePluginQuick(quickSelectionNode);
		return requestTypePluginF.thenApply(requestTypePlugin -> {
			return requestTypePlugin.map(rType -> {
				RequestContainer request = rType.createNewRequest();
				request.setId(UUID.randomUUID().toString());
				plugins.loadRequestAspectPlugins().forEach(p -> p.initializeRequestAspects(request));
				return request;
			});
		});
		
	}


	private Optional<RequestTypePlugin> getRequestTypePlugin(boolean useDefault) {
		List<RequestTypePlugin> requestTypePlugins = plugins.loadRequestTypePlugins();
		
		if (requestTypePlugins.size() == 0)
			throw new IllegalArgumentException("No RequestType plugins found");
		
		if (requestTypePlugins.size() == 1 || useDefault)
			return Optional.of(requestTypePlugins.get(0));

		SelectValueDialog dialog = new SelectValueDialog();
		List<String> reqTypeNames = requestTypePlugins.stream().map(p -> p.getRequestType()).collect(Collectors.toList());
		dialog.showAndWait("New Request", "Select Request Type", Optional.of(reqTypeNames.get(0)), reqTypeNames);
		if (!dialog.isCancelled()) {
			return requestTypePlugins.stream().filter(p -> p.getRequestType().equals(dialog.getInput())).findAny();
		} else {
			return Optional.empty();
		}
		
		
	}
	
	@SneakyThrows
	private CompletableFuture<Optional<RequestTypePlugin>> getRequestTypePluginQuick(Node node) {
		List<RequestTypePlugin> requestTypePlugins = plugins.loadRequestTypePlugins();
		
		if (requestTypePlugins.size() == 0)
			throw new IllegalArgumentException("No RequestType plugins found");
		
		ContextMenu ctxMenu = new ContextMenu();
		CompletableFuture<RequestTypePlugin> f = new CompletableFuture<RequestTypePlugin>();
		requestTypePlugins.forEach(rtp -> {
			var itm = new MenuItem(rtp.getRequestType());
			itm.setOnAction(e -> {
				f.complete(rtp);
			});
			ctxMenu.getItems().add(itm);
		});
		ctxMenu.setOnCloseRequest(e -> f.complete(null));
		
		Point location = MouseInfo.getPointerInfo().getLocation();
		ctxMenu.show(node, location.getX(), location.getY());
		return f.thenApply(Optional::ofNullable);
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
