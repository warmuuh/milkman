package milkman.plugin.sync.git;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import milkman.domain.SyncDetails;
import milkman.ui.plugin.WorkspaceSynchronizer.SynchronizationDetailFactory;
import milkman.utils.fxml.facade.FxmlBuilder;
import milkman.utils.fxml.facade.ValidatablePaswordField;
import milkman.utils.fxml.facade.ValidatableTextField;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class GitSyncDetailFactory implements SynchronizationDetailFactory {

	private ValidatableTextField gitUrl;
	private ValidatableTextField username;
	private ValidatableTextField branch;
	private ValidatablePaswordField passwordOrToken;
	private Label usernameLbl;
	private Label passwordLbl;

	@Override
	public String getName() {
		return "Synchronization via Git";
	}

	@Override
	public Node getSyncDetailsControls() {
		gitUrl = FxmlBuilder.vtext();
		gitUrl.getValidators().add(FxmlBuilder.requiredValidator());
		branch = FxmlBuilder.vtext();
		branch.setText("master");
		branch.getValidators().add(FxmlBuilder.requiredValidator());
		username = FxmlBuilder.vtext();
		username.getValidators().add(FxmlBuilder.requiredValidator());
		passwordOrToken = FxmlBuilder.vpassword();
		passwordOrToken.getValidators().add(FxmlBuilder.requiredValidator());

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