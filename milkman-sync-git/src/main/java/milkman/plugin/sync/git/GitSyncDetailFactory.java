package milkman.plugin.sync.git;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import milkman.domain.SyncDetails;
import milkman.ui.plugin.WorkspaceSynchronizer.SynchronizationDetailFactory;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class GitSyncDetailFactory implements SynchronizationDetailFactory {

	private JFXTextField gitUrl;
	private JFXTextField username;
	private JFXTextField branch;
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
		branch = new JFXTextField();
		branch.setText("master");
		branch.getValidators().add(new RequiredFieldValidator());
		username = new JFXTextField();
		username.getValidators().add(new RequiredFieldValidator());
		passwordOrToken = new JFXPasswordField();
		passwordOrToken.getValidators().add(new RequiredFieldValidator());

		usernameLbl = new Label("Username");
		passwordLbl = new Label("Password/Token");

		gitUrl.textProperty().addListener((obs, old, newValue) -> updateConnectionTypeLabels(newValue));

		return new VBox(new Label("Git Url"), gitUrl, new Label("Branch"), branch, usernameLbl, username, passwordLbl, passwordOrToken);
	}

	private void updateConnectionTypeLabels(String newValue) {
		GitSyncDetails newDetails = new GitSyncDetails(newValue, "", "", "master");
		if (newDetails.isSsh()) {
			usernameLbl.setText("Ssh File");
			username.setPromptText("insert id_rsa file here...");
			if (StringUtils.isBlank(username.getText())) {
				var id_rsa = Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa");
				if (id_rsa.toFile().exists()){
					username.setText(id_rsa.toString());
				}
			}
			passwordLbl.setText("Ssh File Password");
			passwordOrToken.setPromptText("Enter Ssh File password or leave blank...");
		} else {
			usernameLbl.setText("Username");
			username.setPromptText("enter username here...");
			passwordLbl.setText("Password/Token");
			passwordOrToken.setPromptText("Enter password or token here...");
		}
	}

	@Override
	@SneakyThrows
	public SyncDetails createSyncDetails() {
		if (!gitUrl.validate() || !username.validate() || !branch.validate())
			throw new IllegalArgumentException("Missing entries");

		GitSyncDetails syncDetails = new GitSyncDetails(gitUrl.getText(), username.getText(), passwordOrToken.getText(), branch.getText());
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