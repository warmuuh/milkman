package milkman.plugin.sync.git;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
	public void synchronize(boolean isPush, Workspace workspace) {
		GitSyncDetails syncDetails = (GitSyncDetails) workspace.getSyncDetails();
		
		//step1: update remote copy
		File syncDir = new File("sync/"+workspace.getWorkspaceId()+"/");
		Git repo = refreshRepository(syncDetails, syncDir);
		ObjectMapper mapper = createMapper();
		
		//step 2: sync
		File collectionFile = new File(syncDir, "collections.json");
		
		if (isPush) {
			mapper.writeValue(collectionFile, workspace.getCollections());
			repo.add()
				.addFilepattern(".")
				.call();
			repo.commit()
				.setMessage("milkman sync")
				.call();
			repo.push()
				.setCredentialsProvider(creds(syncDetails))
				.call();
		} else { // pull
			List<Collection> remoteCollections = mapper.readValue(collectionFile, new TypeReference<List<Collection>>() {});
			workspace.setCollections(remoteCollections);
		}
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
