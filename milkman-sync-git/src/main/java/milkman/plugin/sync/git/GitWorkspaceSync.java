package milkman.plugin.sync.git;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.danielbechler.diff.node.DiffNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Workspace;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.plugin.WorkspaceSynchronizer;

/**
 * very easy way of syncing by either pushing/ pulling changes manually.
 * there is no sync actually going on like diffing/merging
 * 
 * TODO: need to implement some kind of Differential Synchronization using java-object-diff (see Difftest.java).
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

		List<Collection> commonCopy = new LinkedList<Collection>();
		if (collectionFile.exists())
			commonCopy = mapper.readValue(collectionFile, new TypeReference<List<Collection>>() {});
		
		
		Git repo = refreshRepository(syncDetails, syncDir);
		
		List<Collection> serverCopy = new LinkedList<Collection>();
		if (collectionFile.exists())
			serverCopy = mapper.readValue(collectionFile, new TypeReference<List<Collection>>() {});
		
		List<Collection> workingCopy = workspace.getCollections();
		
		//step1: compute diff against common copy
		DiffNode workingCopyChanges = diffMerger.compare(workingCopy, commonCopy);

		//step2: merge diffs to server copy
		if (workingCopyChanges.isChanged()) {
			diffMerger.mergeDiffs(workingCopy, serverCopy, workingCopyChanges);
			mapper.writeValue(collectionFile, serverCopy);
			repo.add()
				.addFilepattern(".")
				.call();
			repo.commit()
				.setMessage("milkman sync")
				.call();
			repo.push()
				.setCredentialsProvider(creds(syncDetails))
				.call();
		}
		
		
		//step3: merge server-diffs to working copy
		workspace.setCollections(serverCopy);
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
			repo = Git.cloneRepository()
				.setURI(syncDetails.getGitUrl())
				.setCredentialsProvider(creds(syncDetails))
				.setDirectory(syncDir)
				.setCloneAllBranches(true)
				.setBranch("master")
				.call();
			
			
		} else {
			repo = Git.open(syncDir);
			repo.pull()
				.setCredentialsProvider(creds(syncDetails))
				.call();
		}
		return repo;
	}

	private UsernamePasswordCredentialsProvider creds(GitSyncDetails syncDetails) {
		return new UsernamePasswordCredentialsProvider(syncDetails.getUsername(), syncDetails.getPasswordOrToken());
	}

	@Override
	public SynchronizationDetailFactory getDetailFactory() {
		return new GitSyncDetailFactory();
	}

}
