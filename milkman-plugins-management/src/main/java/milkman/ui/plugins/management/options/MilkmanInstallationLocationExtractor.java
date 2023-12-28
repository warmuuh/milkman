package milkman.ui.plugins.management.options;

import java.io.File;
import java.util.Optional;

import lombok.experimental.UtilityClass;
import milkman.MilkmanApplication;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class MilkmanInstallationLocationExtractor {

	private static final String OVERRIDE_PROPERTY_NAME = "milkman.installation.location";

	public static String getMilkmanInstallationDirectory() {
		return Optional
			.ofNullable(System.getProperty(OVERRIDE_PROPERTY_NAME))
			.orElseGet(MilkmanInstallationLocationExtractor::findMilkmanInstallationDirectory);
	}

	private static String findMilkmanInstallationDirectory() {
		if (runningFromJar()) {
			return getCurrentJarDirectory();
		}
		return getCurrentProjectDirectory();
	}

	private static boolean runningFromJar() {
		return StringUtils.endsWithIgnoreCase(getJarName(), ".jar");
	}

	private static String getJarName() {
		return getLocationForClass();
	}

	private static String getCurrentJarDirectory() {
		return new File(getLocationForClass()).getParent();
	}

	private static String getLocationForClass() {
		return new File(MilkmanApplication.class
			.getProtectionDomain()
			.getCodeSource()
			.getLocation()
			.getPath())
			.getName();
	}

	private static String getCurrentProjectDirectory() {
		return new File("").getAbsolutePath();
	}

}
