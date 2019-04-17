package milkman.plugin.sync.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import milkman.domain.SyncDetails;
import milkman.ui.plugin.WorkspaceSynchronizer.SynchronizationDetailFactory;

public class GitSyncDetailFactory implements SynchronizationDetailFactory {

	private JFXTextField gitUrl;
	private JFXTextField username;
	private JFXPasswordField passwordOrToken;

	@Override
	public String getName() {
		return "Synchronization via Git";
	}

	@Override
	public Node getSyncDetailsControls() {
		gitUrl = new JFXTextField();
		gitUrl.getValidators().add(new RequiredFieldValidator());
		username = new JFXTextField();
		username.getValidators().add(new RequiredFieldValidator());
		passwordOrToken = new JFXPasswordField();
		passwordOrToken.getValidators().add(new RequiredFieldValidator());
		
		return new VBox(
					new Label("Git Url"), gitUrl,
					new Label("Username"), username,
					new Label("Password/Token"), passwordOrToken
				);
	}

	@Override
	@SneakyThrows
	public SyncDetails createSyncDetails() {
		if (!gitUrl.validate() || !username.validate() || !passwordOrToken.validate())
			throw new IllegalArgumentException("Missing entries");
		
		//validate by reading
		Git.lsRemoteRepository()
                .setHeads(true)
                .setTags(true)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username.getText(), passwordOrToken.getText()))
                .setRemote(gitUrl.getText())
            	.call();
		
		
		
		return new GitSyncDetails(gitUrl.getText(), username.getText(), passwordOrToken.getText());
	}
}