package milkman;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
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

	public static KeyCombination getControlKeyCombination(KeyCode keyCode){
		Modifier controlKey = KeyCombination.CONTROL_DOWN;
		if (SystemUtils.IS_OS_MAC){
			controlKey = KeyCombination.META_DOWN;
		}
		return new KeyCodeCombination(keyCode, controlKey);
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

	public static boolean tryOpenBrowser(String url) {
		if (SystemUtils.IS_OS_LINUX) {
			// Workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
				return true;
			} catch (IOException e) {
				log.warn("Failed to open browser via xdg-open", e);
			}
		} else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(URI.create(url));
				return true;
			} catch (IOException e) {
				log.warn("Failed to open browser", e);
			}
		} else {
			log.warn("Unsupported plateform, can't launch the browser");
		}
		return false;
	}

}
