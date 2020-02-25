package milkman.utils.fxml;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

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

	public static JFXAlert createDialog(JFXDialogLayout content) {
		JFXAlert dialog = new JFXAlert(primaryStage);
		dialog.setContent(content);
		dialog.setOverlayClose(false);
		dialog.initModality(Modality.APPLICATION_MODAL);
		return dialog;
	}
	
}
