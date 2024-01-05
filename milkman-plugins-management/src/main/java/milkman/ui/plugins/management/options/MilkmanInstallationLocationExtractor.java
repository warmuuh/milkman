package milkman.ui.plugins.management.options;

import java.io.File;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import milkman.MilkmanApplication;
import org.apache.commons.io.FilenameUtils;

@UtilityClass
public class MilkmanInstallationLocationExtractor {

  private static final String OVERRIDE_PROPERTY_NAME = "milkman.installation.location";

  public static String getMilkmanInstallationDirectory() {
    return Optional
        .ofNullable(System.getProperty(OVERRIDE_PROPERTY_NAME))
        .orElseGet(MilkmanInstallationLocationExtractor::findMilkmanInstallationDirectory);
  }

  private static String findMilkmanInstallationDirectory() {
    File jarFile = getLocationForClass(MilkmanApplication.class);

    if (FilenameUtils.isExtension(jarFile.getPath(), "jar")) {
      return jarFile.getParent();
    }
    return getCurrentProjectDirectory();
  }


  private static File getLocationForClass(Class<?> clazz) {
    return new File(clazz
        .getProtectionDomain()
        .getCodeSource()
        .getLocation()
        .getPath());
  }

  private static String getCurrentProjectDirectory() {
    return new File("").getAbsolutePath();
  }

}
