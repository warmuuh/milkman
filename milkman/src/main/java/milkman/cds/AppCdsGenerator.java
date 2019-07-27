package milkman.cds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.ctrl.RequestTypeManager;
import milkman.ctrl.SynchManager;
import milkman.ctrl.WorkspaceController;
import milkman.persistence.PersistenceManager;
import milkman.ui.main.HotkeyManager;
import milkman.ui.main.Toaster;
import milkman.ui.main.ToolbarComponent;
import milkman.ui.plugin.UiPluginManager;
import milkman.update.UpdateChecker;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class AppCdsGenerator {

	private final Toaster toaster;
	
	public void initializeCds(boolean forceRefresh) {

		
		new Thread(() -> {
			
			if (!PlatformUtil.isCurrentDirWritable()) {
				log.error("Current directory is not writeable, please restart milkman as admin");
				return;
			}
			
			var oldCdsArchiveName = new File("app-cds-old.jsa");
			if (oldCdsArchiveName.exists()) {
				oldCdsArchiveName.delete();
			}
			
			String classPath = getClassPath();
			
			if (!isValidClassPath(classPath)) {
				log.error("Only a Jar-only classpath is allowed. Did you start app in an IDE? classpath was: {}", classPath);
				return;
			}
			
			if (!forceRefresh && !isRegenrationNecessary(classPath)) {
				if (isSharedArchiveUsable(classPath)) {
					log.info("AppCds archive is up-to-date and usable. Not regenerating.");
					return;
				} else {
					log.warn("AppCds archive not usable. renaming so it will be deleted on next run and regenerated");
					invalidateCdsArchive();
				}
			}
			
			
			regenerateAppCdsArchive(classPath);
		}).start();
	}

	public void invalidateCdsArchive() {
		var oldCdsArchiveName = new File("app-cds-old.jsa");
		try {
			FileUtils.moveFile(new File("app-cds.jsa"), oldCdsArchiveName);
		} catch (IOException e) {
			toaster.showToast("Failed to reset startup optimization. please remove app-cds.jsa manually");
			log.warn("Failed to rename file. Please remove app-cds.jsa manually", e);
			return;
		}
	}

	protected void regenerateAppCdsArchive(String classPath) {
		try {
			log.info("Regenerating AppCds Archive.");
			Process process = new ProcessBuilder(
					getJavaExecutable(), 
					"-Xshare:dump", 
					"-XX:SharedClassListFile=classes.lst", 
					"-XX:SharedArchiveFile=app-cds.jsa",
					"-XX:+UseCompressedOops", 
					"-XX:+UseCompressedClassPointers",
					"-cp", classPath
					).redirectOutput(new File("appcds.log"))
					.redirectError(new File("appcds.log"))
					.start();
			int exitStatus = process.waitFor();
			if (exitStatus == 0) {
				log.info("AppCds file generation done.");
				storeClasspath(classPath);
				toaster.showToast("Startup Optimization....done.");
			} else {
				log.error("Failed to generate AppCds file");
			}
		} catch (Exception e) {
			log.error("Executing AppCds generation failed", e);
		}
	}
	
	
	private boolean isSharedArchiveUsable(String classpath) {
		try {
			Process process = new ProcessBuilder(
					getJavaExecutable(), 
					"-Xshare:on", 
					"-XX:SharedArchiveFile=app-cds.jsa",
					"-client",
					"-XX:+UseCompressedOops", 
					"-XX:+UseCompressedClassPointers",
					"-cp", classpath,
					getClass().getName() // AppCdsGenerator::main
					).start();
			int exitStatus = process.waitFor();
			if (exitStatus == 0) {
				//app could be started with shared archive, everything is ok
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isRegenrationNecessary(String classPath) {
		File oldAppCdsClasspathFile = new File("app-cds.classpath");
		if (!oldAppCdsClasspathFile.exists())
			return true;
		
		try {
			String oldAppCdsClasspath = FileUtils.readFileToString(oldAppCdsClasspathFile);
			//any changes to classpath?
			if (!classPath.startsWith(oldAppCdsClasspath))
				return true;
		} catch (IOException e) {
			log.error("Cannot read app-cds.classpath file");
		}
		
		var appCdaArchiveFile = new File("app-cds.jsa");
		
		return !appCdaArchiveFile.exists();
	}

	
	public static boolean isFilelocked(File file) {
		 try {
	         try (FileInputStream in = new FileInputStream(file)) {
	             in.read();
	             System.out.println("could read file");
	             return false;
	         }
	     } catch (FileNotFoundException e) {
	         return file.exists();
	     } catch (IOException ioe) {
	         return true;
	     }
    }
	
	private void storeClasspath(String classpath) {
		try {
			FileUtils.write(new File("app-cds.classpath"), classpath);
		} catch (IOException e) {
			log.error("Cannot write app-cds.classpath file");
		}
	}
	
	private boolean isValidClassPath(String classPath) {
		return classPath.length() > 0;
	}

	private String getClassPath() {
		return System.getProperty("java.class.path");
//		return Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator))
//			.stream()
//			.filter(e -> e.endsWith("jar"))
//			.collect(Collectors.joining(File.pathSeparator));
	}

	private String getJavaExecutable() {
		String executable = "java";
		if (SystemUtils.IS_OS_WINDOWS) {
			executable = "java.exe";
		}
		return Paths.get(System.getProperty("java.home"), "bin", executable).toString();
	}
	
	
	public static void main(String[] args) { 
		/* will be called in AppCdsGenerator::isSharedArchiveUsable to see if archive can be used and is valid */
	}
}
