package milkman.ui.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import javax.inject.Singleton;

@Singleton
public class UiPluginManager {

	public List<RequestAspectsPlugin> loadRequestAspectPlugins(){
		ServiceLoader<RequestAspectsPlugin> loader = ServiceLoader.load(RequestAspectsPlugin.class);
		
		List<RequestAspectsPlugin> result = new LinkedList<RequestAspectsPlugin>();
		loader.forEach(result::add);
		
		return result; 
	}
	
	
	public List<RequestTypePlugin> loadRequestTypePlugins(){
		ServiceLoader<RequestTypePlugin> loader = ServiceLoader.load(RequestTypePlugin.class);
		
		List<RequestTypePlugin> result = new LinkedList<RequestTypePlugin>();
		loader.forEach(result::add);
		
		return result; 
	}
}
