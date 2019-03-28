package milkman.ui.plugin;

import java.util.List;

public interface UiRequestAspectsPlugin {

	List<TabController> getRequestTabs();
	
	List<TabController> getResponseTabs();
		
}
