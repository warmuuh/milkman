//package milkman.persistence;
//
//import lombok.extern.slf4j.Slf4j;
//import milkman.domain.Workspace;
//
//import javax.inject.Singleton;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Optional;
//import java.util.Random;
//import java.util.stream.Collectors;
//
//// bogus inmemory impl only for graal testing
//@Singleton
//@Slf4j
//public class PersistenceManagerInmemory {
//
//	private List<Workspace> workspaces = new LinkedList<>();
//	private List<OptionEntry> options = new LinkedList<>();
//	private WorkbenchState workbenchState = new WorkbenchState();
//
//	public WorkbenchState loadWorkbenchState() {
//		return workbenchState;
//	}
//	public void saveWorkbenchState(WorkbenchState state) {
//		workbenchState = state;
//	}
//
//	public List<String> loadWorkspaceNames(){
//		return	workspaces.stream().map(Workspace::getName).collect(Collectors.toList());
//	}
//
//	public Optional<Workspace> loadWorkspaceByName(String name) {
//		return workspaces.stream().filter(ws -> ws.getName().equals(name)).findFirst();
//	}
//
//	public void persistWorkspace(Workspace workspace) {
//		if (workspace.getId() == 0) {
//			long newId = new Random().nextLong(); //todo awkward method to generate new id
//			workspace.setId(newId);
//			workspaces.add(workspace);
//		}
//	}
//
//	public List<OptionEntry> loadOptions(){
//		return options;
//	}
//
//	public void storeOptions(List<OptionEntry> optEntries) {
//		options = optEntries;
//	}
//
//	public boolean deleteWorkspace(String workspaceName) {
//		return workspaces.removeIf(ws -> ws.getName().equals(workspaceName));
//	}
//
//
//}
