package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import milkman.ui.components.ContentEditor;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class MainWindow {

	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;
	private final WorkingAreaComponent workingArea;
	private final RequestCollectionComponent requestCollectionComponent;
	private final ToolbarComponent toolbarComponent;
	private final ContentEditor contentEditor;
	


	public void start(Stage primaryStage) throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
			loader.setControllerFactory(this::getController);

			Parent root = loader.load();

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setWidth(1000);
			primaryStage.setHeight(800);

			
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
		if (type.equals(ContentEditor.class))
			return contentEditor;

		return null;
	}

}
