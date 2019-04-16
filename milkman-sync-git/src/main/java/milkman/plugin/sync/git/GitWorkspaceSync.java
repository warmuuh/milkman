package milkman.plugin.sync.git;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.plugin.WorkspaceSynchronizer;

@Slf4j
public class GitWorkspaceSync implements WorkspaceSynchronizer {

	@Override
	public boolean supportSyncOf(Workspace workspace) {
		return workspace.getSyncDetails() instanceof GitSyncDetails;
	}

	@Override
	@SneakyThrows
	public void synchronize(Workspace workspace) {
		GitSyncDetails syncDetails = (GitSyncDetails) workspace.getSyncDetails();
		
		//step1: update remote copy
		File syncDir = new File("sync/"+workspace.getWorkspaceId()+"/");
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
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT); //allows git line-by-line diffs
		
		//step 2: sync
		//TODO: sync with internal state
		File collectionFile = new File(syncDir, "collections.json");

		if (collectionFile.exists()) {
			List<Collection> remoteCollections = mapper.readValue(collectionFile, new TypeReference<List<Collection>>() {});
			DiffNode diffNode = ObjectDifferBuilder.startBuilding()
					.comparison().ofType(RequestContainer.class).toUseEqualsMethodOfValueProvidedByMethod("id").and()
					.build().compare(workspace.getCollections(), remoteCollections);
			
			
			if (diffNode.hasChanges()) {
				diffNode.visit(new DiffNode.Visitor()
				{
				    public void node(DiffNode node, Visit visit)
				    {
				    	if (node.hasChanges() && !node.hasChildren()) {
					        final Object baseValue = node.canonicalGet(remoteCollections);
					        final Object workingValue = node.canonicalGet(workspace.getCollections());
					        final String message = node.getPath() + " changed from " + 
					                               baseValue + " to " + workingValue;
					        System.out.println(message);
				    	}
				    }
				});
			} else {
				System.out.println("No changes");
			}
		}
		
		//step 3: update remote copy:
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
	}

	private UsernamePasswordCredentialsProvider creds(GitSyncDetails syncDetails) {
		return new UsernamePasswordCredentialsProvider(syncDetails.getUsername(), syncDetails.getPasswordOrToken());
	}

	@Override
	public SynchronizationDetailFactory getDetailFactory() {
		return new GitSyncDetailFactory();
	}

}
