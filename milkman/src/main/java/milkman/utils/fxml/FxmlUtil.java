package milkman.utils.fxml;

import com.jfoenix.controls.JFXAlert;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import milkman.utils.fxml.facade.DialogLayoutBase;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class FxmlUtil {

	private static Stage primaryStage;

	public static void setPrimaryStage(Stage primaryStage) {
		FxmlUtil.primaryStage = primaryStage;
	}
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

//	
//	
//	public static Stage createDialog(String title, Parent content){
//		Stage dialogStage = new Stage();
//		dialogStage.setTitle(title);
//		dialogStage.initModality(Modality.WINDOW_MODAL);
////		dialogStage.initOwner(primaryStage); //TODO: to make it modal
//		Scene scene = new Scene(content);
//		dialogStage.setScene(scene);
//		return dialogStage;
//	}
//	
//	
//	public static Dialog createDialog(String title, Region content, List<Node> actions){
//		JFXAlert dialog = new JFXAlert(null);
//		JFXDialogLayout layout = new JFXDialogLayout();
//		layout.setHeading(new Label(title));
//		layout.setBody(content);
//		layout.setActions(actions);
//		dialog.setContent(content);
//		dialog.initModality(Modality.APPLICATION_MODAL);
////		dialog.setTransitionType(DialogTransition.CENTER);
////		Stage dialogStage = new Stage();
////		dialogStage.setTitle(title);
////		dialogStage.initModality(Modality.WINDOW_MODAL);
////		dialogStage.initOwner(primaryStage); //TODO: to make it modal
////		Scene scene = new Scene(content);
////		dialogStage.setScene(scene);
//		return dialog;
//	}

	public static Dialog createDialog(DialogLayoutBase content) {
		JFXAlert dialog = new JFXAlert(primaryStage);
		dialog.setContent(content);
		dialog.setOverlayClose(false);
		dialog.initModality(Modality.APPLICATION_MODAL);
		return dialog;
	}

	/**
	 * This method is used to run a specified Runnable in the FX Application thread,
	 * it waits for the task to finish before returning to the main thread.
	 *
	 * @param doRun This is the sepcifed task to be excuted by the FX Application thread
	 * @return Nothing
	 */
	public static void runInFXAndWait(Runnable doRun) {
		if (Platform.isFxApplicationThread()) {
			doRun.run();
			return;
		}
		CountDownLatch doneLatch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				doRun.run();
			} finally {
				doneLatch.countDown();
			}
		});
		try {
			doneLatch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
}
