package milkman;

import javafx.application.Preloader;
import javafx.stage.Stage;

/**
 * Created by @author mapuo on 28/07/2019.
 */
public class MilkmanPreloader extends Preloader {

    @Override
    public void start(Stage primaryStage) throws Exception {
        com.sun.glass.ui.Application.GetApplication().setName("Milkman");
    }

}
