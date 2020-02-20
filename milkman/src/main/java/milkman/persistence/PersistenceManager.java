package milkman.persistence;

import java.io.File;
import java.util.*;
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
import org.mapdb.*;

@Singleton
@Slf4j
public class PersistenceManager {
	
	private Map<String, Workspace> workspaces;
	private Set<OptionEntry> options;
	private Atomic.Var<WorkbenchState> workbenchState;
	private DB db;

	@PostConstruct
	public void init() {
		String writableLocationForFile = PlatformUtil.getWritableLocationForFile("database.mapdb");
		db = DBMaker.fileDB(new File(writableLocationForFile))
//				.encryptionEnable("milkman")
				.make();


		JacksonMapper nitriteMapper = new JacksonMapper();
		ObjectMapper mapper = nitriteMapper.getObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());

		workspaces = db.hashMap("workspaces", Serializer.STRING, new MapDbJsonSerializer(mapper, Workspace.class));
		workbenchState = db.atomicVar("workbenchstate", new MapDbJsonSerializer(mapper, WorkbenchState.class));
		workbenchState = new Atomic.Var<>(db.getEngine(), workbenchState.getRecid(), new MapDbJsonSerializer(mapper, WorkbenchState.class));
		options = db.hashSet("options",  new MapDbJsonSerializer(mapper, OptionEntry.class));
	}

	public WorkbenchState loadWorkbenchState() {
		return Optional.ofNullable(workbenchState.get()).orElse(new WorkbenchState());
	}
	public void saveWorkbenchState(WorkbenchState state) {
		workbenchState.set(state);
	}
	
	public List<String> loadWorkspaceNames(){
		return new LinkedList<>(workspaces.keySet());
	}

	public Optional<Workspace> loadWorkspaceByName(String name) {
		return Optional.ofNullable(workspaces.get(name));
	}

	public void persistWorkspace(Workspace workspace) {
		if (workspace.getId() == 0) {
			long newId = new Random().nextLong(); //todo awkward method to generate new id
			workspace.setId(newId);			

		}
		workspaces.put(workspace.getName(), workspace);
		//todo: this breaks functionality, we need to use id as identifier, not name, otherwise, renaming is not possible

	}
	
	public List<OptionEntry> loadOptions(){
		return new LinkedList<>(options);
	}
	
	public void storeOptions(List<OptionEntry> optEntries) {
		options.clear();
		options.addAll(optEntries);
	}

	public boolean deleteWorkspace(String workspaceName) {
		return workspaces.remove(workspaceName) != null;
	}


	public void close() {
		db.commit();
		db.close();
	}
}
