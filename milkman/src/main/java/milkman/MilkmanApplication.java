package milkman;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.main.options.CoreApplicationOptionsProvider;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MilkmanApplication extends Application {

	private MainModule module;
	private CompletableFuture<Void> optionsLoad;

	private static long startInitializationTime;
	private boolean hasError = false;

	@Override
	public void init() throws Exception {
		super.init();
		long t0 = System.currentTimeMillis();
		module = new MainModule();
		log.debug("[startup] MainModule construction: {} ms", System.currentTimeMillis() - t0);
		try {
			long t1 = System.currentTimeMillis();
			module.start();
			log.debug("[startup] MainModule.start (DI + @PostConstruct): {} ms", System.currentTimeMillis() - t1);
			// Options load hits Nitrite + Jackson; kick it off now so it overlaps
			// with MainWindow scene construction on the JavaFX thread.
			optionsLoad = CompletableFuture.runAsync(() -> {
				long t2 = System.currentTimeMillis();
				module.getApplicationController().initOptions();
				log.debug("[startup] initOptions (async): {} ms", System.currentTimeMillis() - t2);
			});
		} catch (Throwable t) {
			hasError = true;
			Platform.runLater(() -> showFatalError(t));
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		if (!hasError) {
			try {
				long t;
				t = System.currentTimeMillis();
				module.getMainWindow().start(primaryStage);
				log.debug("[startup] MainWindow.start: {} ms", System.currentTimeMillis() - t);

				t = System.currentTimeMillis();
				optionsLoad.join();
				log.debug("[startup] await initOptions: {} ms", System.currentTimeMillis() - t);

				t = System.currentTimeMillis();
				module.getThemeSwitcher().setTheme(CoreApplicationOptionsProvider.options().getTheme(), CoreApplicationOptionsProvider.options().isDisableAnimations());
				log.debug("[startup] setTheme: {} ms", System.currentTimeMillis() - t);

				t = System.currentTimeMillis();
				module.getApplicationController().initApplication();
				log.debug("[startup] initApplication: {} ms", System.currentTimeMillis() - t);

				log.info("App Initialization time: {} ms", System.currentTimeMillis() - startInitializationTime);

				
				primaryStage.setOnCloseRequest(e -> {
					module.getApplicationController().tearDown();
				});
			} catch (Throwable t) {
				hasError = true;
				showFatalError(t);
			}
		}
	}

	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		log.info("VM Startup time: {} ms", ManagementFactory.getRuntimeMXBean().getUptime());
		startInitializationTime = System.currentTimeMillis();
		System.setProperty("javafx.preloader", MilkmanPreloader.class.getCanonicalName());
		Application.launch(MilkmanApplication.class, args);
	}

	@SneakyThrows
	private static void showFatalError(Throwable t) {
		log.error("Failed to start application", t);
		Alert alert = new ExceptionDialog(t);
		alert.showAndWait();
		Platform.exit();
	}

}
