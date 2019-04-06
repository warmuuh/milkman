package milkman.ui.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import javax.inject.Singleton;

@Singleton
public class UiPluginManager {

	public List<RequestAspectsPlugin> loadRequestAspectPlugins() {
		ServiceLoader<RequestAspectsPlugin> loader = ServiceLoader.load(RequestAspectsPlugin.class);

		List<RequestAspectsPlugin> result = new LinkedList<RequestAspectsPlugin>();
		loader.forEach(result::add);

		return result;
	}

	public List<RequestTypePlugin> loadRequestTypePlugins() {
		ServiceLoader<RequestTypePlugin> loader = ServiceLoader.load(RequestTypePlugin.class);

		List<RequestTypePlugin> result = new LinkedList<RequestTypePlugin>();
		loader.forEach(result::add);

		return result;
	}

	public List<ImporterPlugin> loadImporterPlugins() {
		ServiceLoader<ImporterPlugin> loader = ServiceLoader.load(ImporterPlugin.class);

		List<ImporterPlugin> result = new LinkedList<ImporterPlugin>();
		loader.forEach(result::add);

		return result;
	}
	
	public List<ContentTypePlugin> loadContentTypePlugins(){
		ServiceLoader<ContentTypePlugin> loader = ServiceLoader.load(ContentTypePlugin.class);

		List<ContentTypePlugin> result = new LinkedList<ContentTypePlugin>();
		loader.forEach(result::add);

		return result;
	}
}
