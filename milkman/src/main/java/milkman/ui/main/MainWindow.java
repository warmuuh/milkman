package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.ui.plugin.UiThemePlugin;
import milkman.utils.fxml.FxmlUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class MainWindow {

	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;
	private final WorkingAreaComponent workingArea;
	private final RequestCollectionComponent requestCollectionComponent;
	private final ToolbarComponent toolbarComponent;
	private final HotkeyManager hotkeys;
	@Getter @FXML StackPane root;
	private Scene mainScene;
	


	public void start(Stage primaryStage) throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
			loader.setControllerFactory(this::getController);

			StackPane root = loader.load();
			FxmlUtil.setPrimaryStage(primaryStage);

			mainScene = new Scene(root);
			
			hotkeys.registerGlobalHotkeys(mainScene);
			
			primaryStage.setScene(mainScene);
			primaryStage.setWidth(1000);
			primaryStage.setHeight(800);

			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
			
//			CSSFX.start(primaryStage);
			
			
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

	public void switchToTheme(UiThemePlugin theme) {

		mainScene.getStylesheets().clear();
		mainScene.getStylesheets().add(getClass().getResource(theme.getMainCss()).toExternalForm());
		mainScene.getStylesheets().add(getClass().getResource(theme.getCodeCss()).toExternalForm());

	}

}
