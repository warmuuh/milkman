package milkman.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import lombok.val;
import milkman.domain.Workspace;

@Singleton
public class PersistenceManager {
	
	private Nitrite db;
	private ObjectRepository<Workspace> workspaces;


	public List<String> loadWorkspaceNames(){
		List<String> result = new LinkedList<String>();
		for(val workspace : workspaces.find())
			result.add(workspace.getName());
		return result;
	}

	public Optional<Workspace> loadWorkspaceByName(String name) {
		return Optional.ofNullable(workspaces
				.find(ObjectFilters.eq("name", name))
				.firstOrDefault());
	}

	
	public void persistWorkspace(Workspace workspace) {
		if (workspace.getId() == 0) {
			workspace.setId(workspaces.size()+1);
			workspaces.insert(workspace);
		}
		else
			workspaces.update(workspace);
	}
	
	
	@PostConstruct
	public void init() {
		db = Nitrite.builder()
		        .compressed()
		        .filePath("database.db")
		        .openOrCreate("milkman", "bringthemilk");

		workspaces = db.getRepository(Workspace.class);
	}
	
	
}
