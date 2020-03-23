package milkman.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.main.Toaster;
import milkman.utils.VersionLoader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class UpdateChecker {

	private final Toaster toaster;
	private String currentVersion;
	
	private GithubReleaseChecker releaseChecker = new GithubReleaseChecker("warmuuh", "milkman");
	
	public void checkForUpdateAsync() {
		new Thread(() -> {
			try {
				releaseChecker.getNewerRelease(currentVersion).ifPresent(newVersion -> {
					toaster.showToast("New Version available: Milkman " + newVersion, "What's new?", e -> {
						try {
							Desktop.getDesktop().browse(new URI("https://github.com/warmuuh/milkman/blob/master/changelog.md"));
						} catch (IOException | URISyntaxException e1) {
							e1.printStackTrace();
						}
					});
				});
			} catch (IOException e) {
				log.error("Failed to fetch release information", e);
			}
		}).run();
	}
	
	
	@PostConstruct
	public void loadCurrentVersion() {
    	currentVersion = VersionLoader.loadCurrentVersion();
	}
	
}
