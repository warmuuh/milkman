package milkman;


import org.fxmisc.cssfx.CSSFX;

import javafx.application.Application;
import javafx.stage.Stage;

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
		module.getMainWindow().start(primaryStage);
		module.getApplicationController().initApplication();
		primaryStage.setOnCloseRequest(e -> module.getApplicationController().persistState());
	}

	
	
	public static void main(String[] args) {
		launch(args);
	}

}
