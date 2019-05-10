package milkman;



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
		module.getApplicationController().initOptions();
		module.getMainWindow().start(primaryStage);
		module.getThemeSwitcher().setTheme(CoreApplicationOptionsProvider.options().getTheme());
		module.getApplicationController().initApplication();
		primaryStage.setOnCloseRequest(e -> module.getApplicationController().persistState());
	}

	
	
	public static void main(String[] args) {
		launch(args);
	}

}
