package milkman.plugin.sync.git;

import static milkman.plugin.sync.git.JGitUtil.initWith;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.danielbechler.diff.node.DiffNode;
import lombok.SneakyThrows;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.plugin.WorkspaceSynchronizer;
/**
 * some simplistic diff-sync way of synchronizing workspace with remote
 * 
 * @author peter
 *
 */
public class GitWorkspaceSync implements WorkspaceSynchronizer {

	@Override
	public boolean supportSyncOf(Workspace workspace) {
		return workspace.getSyncDetails() instanceof GitSyncDetails;
	}

	@Override
	@SneakyThrows
	public void synchronize(Workspace workspace) {
		GitSyncDetails syncDetails = (GitSyncDetails) workspace.getSyncDetails();
		ObjectMapper mapper = createMapper();
		CollectionDiffer diffMerger = new CollectionDiffer();

		
		File syncDir = new File("sync/"+workspace.getWorkspaceId()+"/");

		File collectionFile = new File(syncDir, "collections.json");
		List<Collection> collectionCommonCopy = new LinkedList<Collection>();
		if (collectionFile.exists())
			collectionCommonCopy = mapper.readValue(collectionFile, new TypeReference<List<Collection>>() {});
		
		File environmentFile = new File(syncDir, "environments.json");
		List<Environment> environmentCommonCopy = new LinkedList<Environment>();
		if (environmentFile.exists())
			environmentCommonCopy = mapper.readValue(environmentFile, new TypeReference<List<Environment>>() {});
		
		
		
		Git repo = refreshRepository(syncDetails, syncDir);
		
		List<Collection> collectionServerCopy = new LinkedList<Collection>();
		if (collectionFile.exists())
			collectionServerCopy = mapper.readValue(collectionFile, new TypeReference<List<Collection>>() {});
		
		List<Environment> environmentServerCopy = new LinkedList<Environment>();
		if (environmentFile.exists())
			environmentServerCopy = mapper.readValue(environmentFile, new TypeReference<List<Environment>>() {});
		
		
		

		List<Collection> collectionWorkingCopy = workspace.getCollections();
		List<Environment> environmentWorkingCopy = workspace.getEnvironments();
		
		//step1: compute diff against common copy
		DiffNode collectionWorkingCopyChanges = diffMerger.compare(collectionWorkingCopy, collectionCommonCopy);
		DiffNode environmentWorkingCopyChanges = diffMerger.compareEnvs(environmentWorkingCopy, environmentCommonCopy);

		//step2: merge diffs to server copy
		if (collectionWorkingCopyChanges.isChanged() || environmentWorkingCopyChanges.isChanged()) {
			diffMerger.mergeDiffs(collectionWorkingCopy, collectionServerCopy, collectionWorkingCopyChanges);
			diffMerger.mergeDiffsEnvs(environmentWorkingCopy, environmentServerCopy, environmentWorkingCopyChanges);
			mapper.writeValue(collectionFile, collectionServerCopy);
			mapper.writeValue(environmentFile, environmentServerCopy);
			repo.add()
				.addFilepattern(".")
				.call();
			repo.commit()
				.setMessage("milkman sync")
				.call();
			initWith(repo.push(), syncDetails)
				.call();
		}
		
		
		//step3: merge server-diffs to working copy
		workspace.setCollections(collectionServerCopy);
		workspace.setEnvironments(environmentServerCopy);
	}

	private ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		mapper.enable(SerializationFeature.INDENT_OUTPUT); //allows git line-by-line diffs
		return mapper;
	}

	private Git refreshRepository(GitSyncDetails syncDetails, File syncDir)
			throws GitAPIException, InvalidRemoteException, TransportException, IOException,
			WrongRepositoryStateException, InvalidConfigurationException, CanceledException, RefNotFoundException,
			RefNotAdvertisedException, NoHeadException {
		Git repo;
		if (!syncDir.exists()) {
			syncDir.mkdirs();
			repo = initWith(Git.cloneRepository(), syncDetails)
				.setURI(syncDetails.getGitUrl())
				.setDirectory(syncDir)
				.setCloneAllBranches(true)
				.setBranch("master")
				.call();
			
			
		} else {
			repo = Git.open(syncDir);
			initWith(repo.pull(), syncDetails)
				.call();
		}
		return repo;
	}


	@Override
	public SynchronizationDetailFactory getDetailFactory() {
		return new GitSyncDetailFactory();
	}

}
