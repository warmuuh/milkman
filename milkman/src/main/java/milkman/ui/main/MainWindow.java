package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.fxmisc.cssfx.CSSFX;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.ui.components.ContentEditor;
import milkman.utils.fxml.FxmlUtil;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class MainWindow {

	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;
	private final WorkingAreaComponent workingArea;
	private final RequestCollectionComponent requestCollectionComponent;
	private final ToolbarComponent toolbarComponent;
	@Getter @FXML StackPane root;
	


	public void start(Stage primaryStage) throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
			loader.setControllerFactory(this::getController);

			StackPane root = loader.load();
			FxmlUtil.setPrimaryStage(primaryStage);

			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/themes/milkman.css").toExternalForm());
			scene.getStylesheets().add(getClass().getResource("/themes/syntax/milkman-syntax.css").toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.setWidth(1000);
			primaryStage.setHeight(800);

			
			CSSFX.start(primaryStage);
			
			
			
			primaryStage.show();
			primaryStage.setTitle("Milkman");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object getController(Class<?> type) {
		if (type.equals(MainWindow.class))
			return this;
		if (type.equals(RequestComponent.class))
			return restRequestComponent;
		if (type.equals(ResponseComponent.class))
			return responseComponent;
		if (type.equals(WorkingAreaComponent.class))
			return workingArea;
		if (type.equals(RequestCollectionComponent.class))
			return requestCollectionComponent;
		if (type.equals(ToolbarComponent.class))
			return toolbarComponent;

		return null;
	}

}
