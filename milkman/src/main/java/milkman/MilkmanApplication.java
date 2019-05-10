package milkman;



import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import milkman.ui.main.options.CoreApplicationOptionsProvider;

public class MilkmanApplication extends Application {
	
	private MainModule module;

	@Override
	public void init() throws Exception {
		super.init();
		module = new MainModule();
		module.start();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		module.getMainWindow().start(primaryStage); // 1 sec
		module.getApplicationController().initOptions();
		module.getThemeSwitcher().setTheme(CoreApplicationOptionsProvider.options().getTheme());
		module.getApplicationController().initApplication(); // 1 sec
		primaryStage.setOnCloseRequest(e -> module.getApplicationController().persistState());
	}

	
	
	public static void main(String[] args) throws IOException {
//		System.out.println("Press Enter to Start..."); // for performance measurements
//		System.in.read();
		launch(args);
	}

}
