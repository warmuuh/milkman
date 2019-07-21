package milkman;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

public class PlatformUtil {

	public static boolean isCurrentDirWritable() {
		return Files.isWritable(new File(".").toPath());
	}
	
	public static String getWritableLocationForFile(String filename) {
		if (Files.isWritable(new File(".").toPath())) {
			return filename;
		} 
		return getOsSpecificAppDataFolder(filename);
	}

	private static String getOsSpecificAppDataFolder(String filename) {
		if (SystemUtils.IS_OS_WINDOWS) {
			return Paths.get(System.getenv("LOCALAPPDATA"), "Milkman", filename).toString();
		} else if (SystemUtils.IS_OS_MAC) {
			return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Milkman", filename).toString();
		} else if (SystemUtils.IS_OS_LINUX) {
			return Paths.get(System.getProperty("user.home"), ".milkman", filename).toString();
		}
		
		return filename;
		
	}
	
}
