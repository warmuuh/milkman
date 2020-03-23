package milkman.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class VersionLoader {


    public static String loadCurrentVersion() {
        String currentVersion = "0";
        try {
            Properties p = new Properties();
            InputStream is = VersionLoader.class.getResourceAsStream("/META-INF/maven/com.github.warmuuh/milkman/pom.properties");
            if (is != null) {
                p.load(is);
                currentVersion = p.getProperty("version", "");
                if (currentVersion.contains("-"))
                    currentVersion = currentVersion.split("-")[0]; //remove qualifier
            }
        } catch (Exception e) {
            log.warn("Failed to load current version, default to 0");
        }
        return currentVersion;
    }
}
