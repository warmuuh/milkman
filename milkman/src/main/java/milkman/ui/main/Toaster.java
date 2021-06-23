package milkman.ui.main;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import milkman.utils.fxml.facade.Messager;

import javax.inject.Singleton;

@Singleton
public class Toaster {


	private final Messager messager;

	public Toaster(MainWindow mainWindow) {
		this.messager = new Messager(mainWindow);
	}

	
	public void showToast(String message) {
		messager.showToast(message);
	}

	public void showToast(String message, String action, EventHandler<ActionEvent> eventHandler) {
		messager.showToast(message, action, eventHandler);
	}
}
