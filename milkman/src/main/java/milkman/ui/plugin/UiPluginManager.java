package milkman.ui.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.Workspace;
import milkman.ui.components.AutoCompleter;
import milkman.ui.main.ActiveEnvironmentProvider;
import milkman.ui.main.Toaster;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class UiPluginManager {

	private final AutoCompleter completer;
	private final ActiveEnvironmentProvider envProvider;
	private final Supplier<Toaster> toaster;
	private final Supplier<PluginRequestExecutor> pluginRequestExecutor;
	private final Supplier<Workspace> activeWorkspace;
	private final ExecutionListenerManager executionListenerManager;
	
	Map<Class, List> cachedInstances = new HashMap<Class, List>();
	
	public List<RequestAspectsPlugin> loadRequestAspectPlugins() {
		return loadOrderedSpiInstances(RequestAspectsPlugin.class);
	}

	public List<RequestTypePlugin> loadRequestTypePlugins() {
		return loadOrderedSpiInstances(RequestTypePlugin.class);
	}

	public List<ImporterPlugin> loadImporterPlugins() {
		return loadSpiInstances(ImporterPlugin.class);
	}
	
	public List<ContentTypePlugin> loadContentTypePlugins(){
		return loadSpiInstances(ContentTypePlugin.class);
	}
	
	public List<OptionPageProvider> loadOptionPages(){
		return loadOrderedSpiInstances(OptionPageProvider.class);
	}

	public List<KeyEditor> loadKeyEditors(){
		return loadOrderedSpiInstances(KeyEditor.class);
	}

	public List<LibraryPlugin> loadLibraryPlugins(){
		return loadSpiInstances(LibraryPlugin.class);
	}

	public List<UiThemePlugin> loadThemePlugins(){
		return loadSpiInstances(UiThemePlugin.class);
	}

	public List<WorkspaceSynchronizer> loadSyncPlugins(){
		return loadOrderedSpiInstances(WorkspaceSynchronizer.class);
	}

	public List<RequestExporterPlugin> loadRequestExportPlugins(){
		return loadOrderedSpiInstances(RequestExporterPlugin.class);
	}
	
	public List<CollectionExporterPlugin> loadCollectionExportPlugins(){
		return loadOrderedSpiInstances(CollectionExporterPlugin.class);
	}

	public List<WorkspaceExporterPlugin> loadWorkspaceExportPlugins(){
		return loadOrderedSpiInstances(WorkspaceExporterPlugin.class);
	}

	public List<TemplateParameterResolverPlugin> loadTemplaterPlugins(){
		return loadSpiInstances(TemplateParameterResolverPlugin.class);
	}

	public void wireUp(Object o) {
		if (o instanceof ContentTypeAwareEditor) {
			((ContentTypeAwareEditor) o).setContentTypePlugins(loadContentTypePlugins());
		}
		
		if (o instanceof AutoCompletionAware) {
			((AutoCompletionAware) o).setAutoCompleter(completer);
		}

		if (o instanceof ActiveEnvironmentAware) {
			((ActiveEnvironmentAware) o).setActiveEnvironment(envProvider.getActiveEnvironment());
		}

		if (o instanceof ActiveKeySetAware) {
			((ActiveKeySetAware) o).setActiveKeySet(activeWorkspace.get().getActiveKeySet());
		}
		
		if (o instanceof ToasterAware) {
			((ToasterAware) o).setToaster(toaster.get());
		}

		if (o instanceof LifecycleAware) {
			((LifecycleAware) o).onPostConstruct();
		}

		if (o instanceof RequestExecutorAware) {
			((RequestExecutorAware) o).setRequestExecutor(pluginRequestExecutor.get());
		}

		if (o instanceof ExecutionListenerAware) {
			((ExecutionListenerAware) o).setExecutionListenerManager(executionListenerManager);
		}

		if (o instanceof LibraryPluginAware) {
			((LibraryPluginAware)o).setLibraryPlugins(loadLibraryPlugins());
		}

		if (o instanceof RequestTypePluginAware) {
			((RequestTypePluginAware)o).setRequestTypePlugins(loadRequestTypePlugins());
		}
	}
	
	public <T> List<T> loadSpiInstances(Class<T> type) {
		if (cachedInstances.containsKey(type))
			return cachedInstances.get(type);
		
		ServiceLoader<T> loader = ServiceLoader.load(type);
		List<T> result = new LinkedList<T>();
		loader.forEach(result::add);
		result.forEach(this::wireUp);
		
		cachedInstances.put(type, result);
		return result;
	}

	
	public <T extends Orderable> List<T> loadOrderedSpiInstances(Class<T> type) {
		if (cachedInstances.containsKey(type))
			return cachedInstances.get(type);
		
		ServiceLoader<T> loader = ServiceLoader.load(type);
		List<T> result = new LinkedList<T>();
		loader.forEach(result::add);
		
		result.forEach(this::wireUp);
		
		cachedInstances.put(type, result);
		
		result.sort((a,b) -> a.getOrder() - b.getOrder());
		
		return result;
	}

	
}
