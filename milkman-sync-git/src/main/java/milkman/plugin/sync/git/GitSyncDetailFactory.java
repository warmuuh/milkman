package milkman.plugin.sync.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
	private Label usernameLbl;
	private Label passwordLbl;

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

		usernameLbl = new Label("Username");
		passwordLbl = new Label("Password/Token");

		gitUrl.textProperty().addListener((obs, old, newValue) -> updateConnectionTypeLabels(newValue));

		return new VBox(new Label("Git Url"), gitUrl, usernameLbl, username, passwordLbl, passwordOrToken);
	}

	private void updateConnectionTypeLabels(String newValue) {
		GitSyncDetails newDetails = new GitSyncDetails(newValue, "", "");
		if (newDetails.isSsh()) {
			usernameLbl.setText("Ssh File");
			if (StringUtils.isBlank(username.getText())) {
				username.setText(Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toString());
			}
			passwordLbl.setText("Ssh File Password");
		} else {
			usernameLbl.setText("Username");
			passwordLbl.setText("Password/Token");
		}
	}

	@Override
	@SneakyThrows
	public SyncDetails createSyncDetails() {
		if (!gitUrl.validate() || !username.validate())
			throw new IllegalArgumentException("Missing entries");

		GitSyncDetails syncDetails = new GitSyncDetails(gitUrl.getText(), username.getText(), passwordOrToken.getText());
		if (syncDetails.isSsh()) {
			if (!new File(syncDetails.getUsername()).exists()) {
				throw new FileNotFoundException("Ssh file not found: " + syncDetails.getUsername());
			}
		} else {
			if( !passwordOrToken.validate())
				throw new IllegalArgumentException("Missing entries");
		}

		// validate by reading
		JGitUtil.initWith(Git.lsRemoteRepository(), syncDetails)
				.setHeads(true)
				.setTags(true)
				.setRemote(gitUrl.getText())
				.call();

		return syncDetails;
	}
}