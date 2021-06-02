package milkman.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.ui.main.Toaster;
import milkman.utils.VersionLoader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class UpdateChecker {

	private final Toaster toaster;
	private String currentVersion;
	
	private final GithubReleaseChecker releaseChecker = new GithubReleaseChecker("warmuuh", "milkman");
	
	public void checkForUpdateAsync() {
		new Thread(() -> {
			try {
				releaseChecker.getNewerRelease(currentVersion).ifPresent(newVersion -> {
					toaster.showToast("New Version available: Milkman " + newVersion, "What's new?",
							e -> PlatformUtil.tryOpenBrowser("https://github.com/warmuuh/milkman/blob/master/changelog.md"));
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
