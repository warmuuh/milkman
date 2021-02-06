package milkman.plugin.sync.git;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.danielbechler.diff.node.DiffNode;
import lombok.SneakyThrows;
import milkman.PlatformUtil;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Workspace;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.plugin.WorkspaceSynchronizer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static milkman.plugin.sync.git.JGitUtil.initWith;
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
	public void synchronize(Workspace workspace, boolean localSyncOnly) {
		GitSyncDetails syncDetails = (GitSyncDetails) workspace.getSyncDetails();
		ObjectMapper mapper = createMapper();
		CollectionDiffer diffMerger = new CollectionDiffer();

		
		File syncDir = new File(PlatformUtil.getWritableLocationForFile("sync/"+workspace.getWorkspaceId()+"/"));

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
		if (localSyncOnly){
			doReadOnlySync(workspace,
					syncDetails,
					mapper,
					diffMerger,
					collectionFile,
					collectionCommonCopy,
					environmentFile,
					environmentCommonCopy,
					repo,
					collectionServerCopy,
					environmentServerCopy,
					collectionWorkingCopy,
					environmentWorkingCopy);
		} else {
			doNormalDiffSync(workspace,
					syncDetails,
					mapper,
					diffMerger,
					collectionFile,
					collectionCommonCopy,
					environmentFile,
					environmentCommonCopy,
					repo,
					collectionServerCopy,
					environmentServerCopy,
					collectionWorkingCopy,
					environmentWorkingCopy);
		}

	}

	private void doNormalDiffSync(Workspace workspace,
								  GitSyncDetails syncDetails,
								  ObjectMapper mapper,
								  CollectionDiffer diffMerger,
								  File collectionFile,
								  List<Collection> collectionCommonCopy,
								  File environmentFile,
								  List<Environment> environmentCommonCopy,
								  Git repo,
								  List<Collection> collectionServerCopy,
								  List<Environment> environmentServerCopy,
								  List<Collection> collectionWorkingCopy,
								  List<Environment> environmentWorkingCopy) throws IOException, GitAPIException {
		//step1: compute working-diffs against common copy
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

	private void doReadOnlySync(Workspace workspace,
								  GitSyncDetails syncDetails,
								  ObjectMapper mapper,
								  CollectionDiffer diffMerger,
								  File collectionFile,
								  List<Collection> collectionCommonCopy,
								  File environmentFile,
								  List<Environment> environmentCommonCopy,
								  Git repo,
								  List<Collection> collectionServerCopy,
								  List<Environment> environmentServerCopy,
								  List<Collection> collectionWorkingCopy,
								  List<Environment> environmentWorkingCopy) throws IOException, GitAPIException {
		//step1: compute server-diffs against common copy
		DiffNode collectionServerCopyChanges = diffMerger.compare(collectionServerCopy, collectionCommonCopy);
		DiffNode environmentServerCopyChanges = diffMerger.compareEnvs(environmentServerCopy, environmentCommonCopy);

		//step2: merge diffs to working copy
		if (collectionServerCopyChanges.isChanged() || environmentServerCopyChanges.isChanged()) {
			diffMerger.mergeDiffs(collectionServerCopy, collectionWorkingCopy, collectionServerCopyChanges);
			diffMerger.mergeDiffsEnvs(environmentServerCopy, environmentWorkingCopy, environmentServerCopyChanges);
		}


		//step3: apply
		workspace.setCollections(collectionWorkingCopy);
		workspace.setEnvironments(environmentWorkingCopy);
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
				.setBranch(syncDetails.getBranch())
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
