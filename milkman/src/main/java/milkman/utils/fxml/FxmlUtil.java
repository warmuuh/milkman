package milkman.utils.fxml;

import java.util.List;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXDialogLayout;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticType;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import milkman.ui.components.TableEditor;

public class FxmlUtil {

	private static Stage primaryStage;

	public static void setPrimaryStage(Stage primaryStage) {
		FxmlUtil.primaryStage = primaryStage;
	}
	
	@SneakyThrows
	public static <T, R extends Node> R loadAndInitialize(String file, T controller){
		FXMLLoader loader = new FXMLLoader(FxmlUtil.class.getResource(file));
		loader.setControllerFactory(c -> controller);
		R root = loader.load();
		// to prevent GCing of controller
		// see https://github.com/sialcasa/mvvmFX/issues/429#issuecomment-238829854 for more details
		root.setUserData(controller);
		return root;
	}
	
	@SneakyThrows
	public static <T, R> R loadAndInitialize(String file){
		FXMLLoader loader = new FXMLLoader(FxmlUtil.class.getResource(file));
		R root = loader.load();
		return root;
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
		dialog.initModality(Modality.APPLICATION_MODAL);
		return dialog;
	}
	
}
