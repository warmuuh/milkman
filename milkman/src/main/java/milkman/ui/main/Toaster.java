package milkman.ui.main;


import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;

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

	public void showToast(String message, String action, EventHandler<ActionEvent> eventHandler) {
		Platform.runLater(() -> {
			lazyInit();
			snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout(message, action, eventHandler), Duration.seconds(5), null));
		});
	}
}
