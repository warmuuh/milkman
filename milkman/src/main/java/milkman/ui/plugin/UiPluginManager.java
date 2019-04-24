package milkman.ui.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.inject.Singleton;

@Singleton
public class UiPluginManager {

	Map<Class, List> cachedInstances = new HashMap<Class, List>();
	
	public List<RequestAspectsPlugin> loadRequestAspectPlugins() {
		return loadSpiInstances(RequestAspectsPlugin.class);
	}

	public List<RequestTypePlugin> loadRequestTypePlugins() {
		return loadSpiInstances(RequestTypePlugin.class);
	}

	public List<ImporterPlugin> loadImporterPlugins() {
		return loadSpiInstances(ImporterPlugin.class);
	}
	
	public List<ContentTypePlugin> loadContentTypePlugins(){
		return loadSpiInstances(ContentTypePlugin.class);
	}
	
	public List<OptionPageProvider> loadOptionPages(){
		return loadSpiInstances(OptionPageProvider.class);
	}
	

	public List<UiThemePlugin> loadThemePlugins(){
		return loadSpiInstances(UiThemePlugin.class);
	}

	public List<WorkspaceSynchronizer> loadSyncPlugins(){
		return loadSpiInstances(WorkspaceSynchronizer.class);
	}

	public List<RequestExporterPlugin> loadRequestExportPlugins(){
		return loadSpiInstances(RequestExporterPlugin.class);
	}
	
	private <T> List<T> loadSpiInstances(Class<T> type) {
		if (cachedInstances.containsKey(type))
			return cachedInstances.get(type);
		
		ServiceLoader<T> loader = ServiceLoader.load(type);
		List<T> result = new LinkedList<T>();
		loader.forEach(result::add);
		
		cachedInstances.put(type, result);
		return result;
	}

	
}
