package milkman.persistence;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

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

@Singleton
@Slf4j
public class PersistenceManager {
	
	private CompletableFuture<Nitrite> db;
	private ObjectRepository<Workspace> workspaces;
	private ObjectRepository<OptionEntry> options;
	private ObjectRepository<WorkbenchState> workbenchStates;
	
	public WorkbenchState loadWorkbenchState() {
		assureInitialization();
		WorkbenchState wb = workbenchStates.find().firstOrDefault();
		if (wb == null) {
			wb = new WorkbenchState();
			workbenchStates.insert(wb);
		}
		return wb;
	}
	public void saveWorkbenchState(WorkbenchState state) {
		assureInitialization();
		workbenchStates.update(state);
	}
	
	public List<String> loadWorkspaceNames(){
		assureInitialization();
		List<String> result = new LinkedList<String>();
		for(val workspace : workspaces.find())
			result.add(workspace.getName());
		return result;
	}

	public Optional<Workspace> loadWorkspaceByName(String name) {
		assureInitialization();
		return Optional.ofNullable(workspaces
				.find(ObjectFilters.eq("name", name))
				.firstOrDefault())
				.map(this::fixSerialization);
	}

	
	public void persistWorkspace(Workspace workspace) {
		assureInitialization();
		if (workspace.getId() == 0) {
			long newId = new Random().nextLong(); //todo awkward method to generate new id
			workspace.setId(newId);			
			workspaces.insert(workspace);
		}
		else
			workspaces.update(workspace);
	}
	
	public List<OptionEntry> loadOptions(){
		assureInitialization();
		List<OptionEntry> result = new LinkedList<OptionEntry>();
		for(val option : options.find())
			result.add(option);
		return result;
	}
	
	public void storeOptions(List<OptionEntry> optEntries) {
		assureInitialization();
		options.remove(ObjectFilters.ALL);
		//adjust IDs:
		for(int i = 0; i < optEntries.size(); ++i)
			optEntries.get(i).setId(i);
		options.insert(optEntries.toArray(new OptionEntry[] {}));
	}

	public boolean deleteWorkspace(String workspaceName) {
		assureInitialization();
		return workspaces.remove(ObjectFilters.eq("name", workspaceName)).getAffectedCount() > 0;
	}
	
	
	@PostConstruct
	public void init() {
		JacksonMapper nitriteMapper = new JacksonMapper();
		ObjectMapper mapper = nitriteMapper.getObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		db = CompletableFuture.supplyAsync(() -> createOrOpenDb(nitriteMapper));
	}
	
	private Nitrite createOrOpenDb(JacksonMapper nitriteMapper) {

		String writableLocationForFile = PlatformUtil.getWritableLocationForFile("database.db");
		var parent = new File(writableLocationForFile).getAbsoluteFile().getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new RuntimeException("Cannot create database folder " + parent.getAbsolutePath());
			}
		}
		var createdDb = Nitrite.builder()
		        .compressed()
		        .filePath(writableLocationForFile)
		        .nitriteMapper(nitriteMapper)
		        .openOrCreate("milkman", "bringthemilk");
		if (createdDb == null)
			throw new RuntimeException("Cannot create database. Permissions?");
		return createdDb;
	}

	@SneakyThrows
	private void assureInitialization() {
		if (workspaces == null || options == null || workbenchStates == null) {
			workspaces = db.get().getRepository(Workspace.class);
			options = db.get().getRepository(OptionEntry.class);
			workbenchStates = db.get().getRepository(WorkbenchState.class);
		}
	}
	

	private Workspace fixSerialization(Workspace ws) {
		//due to json serialization, the active request is duplicated, but should be references from openRequests
		ws.getOpenRequests().stream()
		.filter(r -> r.getId().equals(ws.getActiveRequest().getId()))
		.findAny()
		.ifPresent(or -> ws.setActiveRequest(or));
		
		return ws;
	}
	
}
