package milkmancli;

import static milkmancli.utils.StringUtil.findMatching;

import java.util.Optional;

import javax.inject.Singleton;

import lombok.Data;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;

@Singleton
@Data
public class CliContext {

	Workspace currentWorkspace;
	Collection currentCollection;
	
	
	public Optional<Collection> findCollectionByIdish(String idish) {
		return findMatching(idish,  currentWorkspace.getCollections(), milkman.domain.Collection::getName);
	}
	
	
	public Optional<RequestContainer> findRequestByIdish(String idish) {
		String[] split = idish.split("/");
		Optional<Collection> col = Optional.ofNullable(currentCollection);
		if (split.length == 2) {
			col = findCollectionByIdish(split[0]);
		}
		String reqName = split[split.length -1];
		return col.flatMap( collection -> findMatching(reqName,  collection.getRequests(), RequestContainer::getName));
	}
	
	
}
