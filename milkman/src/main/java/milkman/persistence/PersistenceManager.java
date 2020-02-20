package milkman.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapper.JacksonMapper;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.domain.Workspace;

// bogus inmemory impl only for graal testing
@Singleton
@Slf4j
public class PersistenceManager {
	
	private List<Workspace> workspaces = new LinkedList<>();
	private List<OptionEntry> options = new LinkedList<>();
	private WorkbenchState workbenchState = new WorkbenchState();
	
	public WorkbenchState loadWorkbenchState() {
		return workbenchState;
	}
	public void saveWorkbenchState(WorkbenchState state) {
		workbenchState = state;
	}
	
	public List<String> loadWorkspaceNames(){
		return	workspaces.stream().map(Workspace::getName).collect(Collectors.toList());
	}

	public Optional<Workspace> loadWorkspaceByName(String name) {
		return workspaces.stream().filter(ws -> ws.getName().equals(name)).findFirst();
	}

	public void persistWorkspace(Workspace workspace) {
		if (workspace.getId() == 0) {
			long newId = new Random().nextLong(); //todo awkward method to generate new id
			workspace.setId(newId);			
			workspaces.add(workspace);
		}
	}
	
	public List<OptionEntry> loadOptions(){
		return options;
	}
	
	public void storeOptions(List<OptionEntry> optEntries) {
		options = optEntries;
	}

	public boolean deleteWorkspace(String workspaceName) {
		return workspaces.removeIf(ws -> ws.getName().equals(workspaceName));
	}

	
}
