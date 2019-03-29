package milkman.utils.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;

public class FxmlUtil {

	@SneakyThrows
	public static <T, R> R loadAndInitialize(String file, T controller){
		FXMLLoader loader = new FXMLLoader(FxmlUtil.class.getResource(file));
		loader.setControllerFactory(c -> controller);
		R root = loader.load();
		return root;
	}
	
	public static Stage createDialog(String title, Parent content){
		Stage dialogStage = new Stage();
		dialogStage.setTitle(title);
		dialogStage.initModality(Modality.WINDOW_MODAL);
//		dialogStage.initOwner(primaryStage); //TODO: to make it modal
		Scene scene = new Scene(content);
		dialogStage.setScene(scene);
		return dialogStage;
	}
	
}
