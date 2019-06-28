package milkman.ui.main;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.ui.main.RequestCollectionComponent.RequestCollectionComponentFxml;
import milkman.ui.main.ToolbarComponent.ToolbarComponentFxml;
import milkman.ui.main.WorkingAreaComponent.WorkingAreaComponentFxml;
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
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
//			loader.setControllerFactory(this::getController);

			StackPane root = new MainWindowFxml(this);
			FxmlUtil.setPrimaryStage(primaryStage);

			mainScene = new Scene(root);
			
			hotkeys.registerGlobalHotkeys(mainScene);
			
			primaryStage.setScene(mainScene);
	        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

			primaryStage.setWidth(Math.min(1000, bounds.getWidth()));
			primaryStage.setHeight(Math.min(800, bounds.getHeight()));

			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
			
//			CSSFX.start(primaryStage);
			
			
			primaryStage.show();
			primaryStage.setTitle("Milkman");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//
//	public Object getController(Class<?> type) {
//		if (type.equals(MainWindow.class))
//			return this;
//		if (type.equals(RequestComponent.class))
//			return restRequestComponent;
//		if (type.equals(ResponseComponent.class))
//			return responseComponent;
//		if (type.equals(WorkingAreaComponent.class))
//			return workingArea;
//		if (type.equals(RequestCollectionComponent.class))
//			return requestCollectionComponent;
//		if (type.equals(ToolbarComponent.class))
//			return toolbarComponent;
//
//		return null;
//	}

	public void switchToTheme(UiThemePlugin theme) {

		mainScene.getStylesheets().clear();
//		mainScene.getStylesheets().add(getClass().getResource(theme.getMainCss()).toExternalForm());
//		mainScene.getStylesheets().add(getClass().getResource(theme.getCodeCss()).toExternalForm());
		mainScene.getStylesheets().add(theme.getMainCss());
		mainScene.getStylesheets().add(theme.getCodeCss());
	}

	
	public static class MainWindowFxml extends StackPane {


		public MainWindowFxml(MainWindow controller) {
			controller.root = this;
			this.setId("root");
			BorderPane borderpane = new BorderPane();
			borderpane.setTop(new ToolbarComponentFxml(controller.toolbarComponent));
			this.getChildren().add(borderpane);
			
			SplitPane splitPane = new SplitPane();
			splitPane.setDividerPositions(0.2);
			
			splitPane.getItems().add(new RequestCollectionComponentFxml(controller.requestCollectionComponent));
			splitPane.getItems().add(new WorkingAreaComponentFxml(controller.workingArea));
			borderpane.setCenter(splitPane);
		}
		
		
	}

	
	
}
