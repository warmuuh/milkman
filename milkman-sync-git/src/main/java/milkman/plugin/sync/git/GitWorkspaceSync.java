package milkman.plugin.sync.git;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.SyncDetails;
import milkman.domain.Workspace;
import milkman.ui.main.sync.NoSyncDetails;
import milkman.ui.plugin.WorkspaceSynchronizer;

@Slf4j
public class GitWorkspaceSync implements WorkspaceSynchronizer {

	@Override
	public boolean supportSyncOf(Workspace workspace) {
		return workspace.getSyncDetails() instanceof GitSyncDetails;
	}

	@Override
	public void synchronize(Workspace workspace) {
		log.info("SYNC" + workspace.getSyncDetails());
	}

	@Override
	public SynchronizationDetailFactory getDetailFactory() {
		return new GitSyncDetailFactory();
	}
	
	public static class GitSyncDetailFactory implements SynchronizationDetailFactory {

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
		public SyncDetails createSyncDetails() {
			if (!gitUrl.validate() || !username.validate() || !passwordOrToken.validate())
				throw new IllegalArgumentException("Missing entries");
			
			return new GitSyncDetails(gitUrl.getText(), username.getText(), passwordOrToken.getText());
		}
	}

}
