package milkmancli;

import static milkmancli.utils.StringUtil.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.persistence.PersistenceManager;
import milkman.persistence.WorkbenchState;
import milkmancli.utils.StringUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class CliContext {


	private final PersistenceManager persistence;
	private List<String> workspaces;
	
	
	private Workspace currentWorkspace;
	private Collection currentCollection;
	private WorkbenchState workbenchState;

	public Optional<Workspace> findWorkspaceByIdish(String idish) {
		return findMatching(idish, workspaces)
				.flatMap(persistence::loadWorkspaceByName);
	}
	
	public Optional<Collection> findCollectionByIdish(String idish) {
		return findCollectionByIdish(idish, currentWorkspace);
	}
	public Optional<Collection> findCollectionByIdish(String idish, Workspace ws) {
		return findMatching(idish,  ws.getCollections(), milkman.domain.Collection::getName);
	}
	
	public Optional<Workspace> findWorkspaceOfRequestIdish(String idish){
		String[] split = idish.split("/");
		if (split.length == 3)
			return findWorkspaceByIdish(split[0]);
		
		return Optional.ofNullable(currentWorkspace);
	}
	
	public Optional<RequestContainer> findRequestByIdish(String idish) {
		Stack<String> splits = new Stack<String>();
		splits.addAll(Arrays.asList(idish.split("/")));
		
		
		if (splits.isEmpty()) {
			throw new IllegalArgumentException("No id given");
		}
		
		String reqName = splits.pop();
		Optional<String> colName = Optional.empty();
		Optional<String> wsName = Optional.empty();
		
		if (!splits.isEmpty()) {
			colName = Optional.ofNullable(splits.pop());
		}
		
		if (!splits.isEmpty()) {
			wsName = Optional.ofNullable(splits.pop());
		}
		
		
		Workspace ws = wsName.flatMap(this::findWorkspaceByIdish).orElse(currentWorkspace);
		if (ws == null) {
			throw new IllegalArgumentException("No workspace given");
		}

		Collection col = colName.flatMap(cn -> findCollectionByIdish(cn, ws)).orElse(currentCollection);
		if (col == null) {
			throw new IllegalArgumentException("No collection given");
		}

		return findMatching(reqName,  col.getRequests(), RequestContainer::getName);
	}

	
	@PostConstruct
	public void init() {
		this.workspaces = persistence.loadWorkspaceNames();
		workbenchState = persistence.loadWorkbenchState();
		setCurrentWorkspace(stringToId(workbenchState.getLoadedWorkspace()));
	}
	
	public void setCurrentWorkspace(String wsName) {
		var oldWsName = "";
		if (this.currentWorkspace != null) {
			oldWsName = this.currentWorkspace.getName();
		}
		
		this.currentWorkspace = findMatching(wsName, workspaces)
				.flatMap(persistence::loadWorkspaceByName)
				.orElseThrow(()  -> new IllegalArgumentException("Workspace '" + wsName + "' not found"));
		this.currentCollection = null;
		
		if (!this.currentWorkspace.getName().equals(oldWsName)) {
			workbenchState.setLoadedWorkspace(this.currentWorkspace.getName());
			persistence.saveWorkbenchState(workbenchState);
		}
	}
	
	public void setCurrentCollection(String colName) {
		this.currentCollection = findMatching(colName,  currentWorkspace.getCollections(), milkman.domain.Collection::getName)
				.orElseThrow(()  -> new IllegalArgumentException("Collection not found " + colName));
	}
	
	public Workspace getCurrentWorkspace() {
		return currentWorkspace;
	}
	
	public Collection getCurrentCollection() {
		return currentCollection;
	}
	
	public List<String> getAvailableWorkspaceNames(){
		return workspaces.stream().map(StringUtil::stringToId).collect(Collectors.toList());
	}
	
	public List<String> getAvailableCollectionNames(){
		if (currentWorkspace == null)
			return Collections.emptyList();
		
		return currentWorkspace.getCollections().stream()
			.map(c -> c.getName())
			.map(StringUtil::stringToId)
			.collect(Collectors.toList());
	}
	
	public List<String> getAvailableRequestNames(){
		if (currentWorkspace == null)
			return Collections.emptyList();
		if (currentCollection == null)
			return Collections.emptyList();
		
		return currentCollection.getRequests().stream()
			.map(r -> r.getName())
			.map(StringUtil::stringToId)
			.collect(Collectors.toList());
	}
	
}
