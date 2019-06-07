package milkman.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.main.Toaster;

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
				if (releaseChecker.hasNewerRelease(currentVersion)) {
					toaster.showToast("New Version available at: github.com/warmuuh/milkman/releases");
				}
			} catch (IOException e) {
				log.error("Failed to fetch release information", e);
			}
		}).run();
	}
	
	
	@PostConstruct
	public void loadCurrentVersion() {
    	currentVersion = "0";
		try {
	        Properties p = new Properties();
	        InputStream is = getClass().getResourceAsStream("/META-INF/maven/com.github.warmuuh/milkman/pom.properties");
	        if (is != null) {
	            p.load(is);
	            currentVersion = p.getProperty("version", "");
	            if (currentVersion.contains("-"))
	            	currentVersion = currentVersion.split("-")[0]; //remove qualifier
	        }
	    } catch (Exception e) {
	    	log.warn("Failed to load current version, default to 0");
	    }
	}
	
}
