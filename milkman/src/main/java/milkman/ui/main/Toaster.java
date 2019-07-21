package milkman.ui.main;


import javax.inject.Inject;
import javax.inject.Singleton;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class Toaster {

	private final MainWindow mainWindow;
	
	private JFXSnackbar snackbar;
	
	
	
	public void lazyInit() {
		if (snackbar == null) {
			snackbar = new JFXSnackbar(mainWindow.getRoot());
			snackbar.setPrefWidth(600);
		}
	}
	
	
	public void showToast(String message) {
		Platform.runLater(() -> { 
			lazyInit();
			snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout(message), Duration.seconds(5), null));
		});
	}
}
