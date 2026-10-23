package milkman.ui.main;

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
import lombok.extern.slf4j.Slf4j;
import milkman.ui.main.RequestCollectionComponent.RequestCollectionComponentFxml;
import milkman.ui.main.ToolbarComponent.ToolbarComponentFxml;
import milkman.ui.main.WorkingAreaComponent.WorkingAreaComponentFxml;
import milkman.ui.plugin.UiThemePlugin;
import milkman.utils.fxml.FxmlUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class MainWindow {

	private final RequestComponent restRequestComponent;
	private final ResponseComponent responseComponent;
	private final WorkingAreaComponent workingArea;
	private final RequestCollectionComponent requestCollectionComponent;
	private final ToolbarComponent toolbarComponent;
	private final HotkeyManager hotkeys;
	@Getter StackPane root;
	private Scene mainScene;
	


	public void start(Stage primaryStage) throws Exception {
		try {
			long t = System.currentTimeMillis();
			StackPane root = new MainWindowFxml(this);
			log.debug("[startup] MainWindowFxml ctor: {} ms", System.currentTimeMillis() - t);
			FxmlUtil.setPrimaryStage(primaryStage);

			t = System.currentTimeMillis();
			mainScene = new Scene(root);
			log.debug("[startup] new Scene(root): {} ms", System.currentTimeMillis() - t);

			t = System.currentTimeMillis();
			hotkeys.registerGlobalHotkeys(mainScene);
			log.debug("[startup] registerGlobalHotkeys: {} ms", System.currentTimeMillis() - t);

			primaryStage.setScene(mainScene);
	        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

			primaryStage.setWidth(Math.min(1000, bounds.getWidth()));
			primaryStage.setHeight(Math.min(800, bounds.getHeight()));

			t = System.currentTimeMillis();
			setIcon(primaryStage);
			log.debug("[startup] setIcon: {} ms", System.currentTimeMillis() - t);

			t = System.currentTimeMillis();
			primaryStage.show();
			log.debug("[startup] primaryStage.show: {} ms", System.currentTimeMillis() - t);
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

	protected void setIcon(Stage primaryStage) {
		var icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
		primaryStage.getIcons().add(icon);

		// AWT Toolkit init + Taskbar.getTaskbar() cost ~80 ms on macOS; defer it so
		// it does not block the first paint. The dock icon still appears before the
		// user can act on the window.
		Thread t = new Thread(() -> {
			try {
				var awtIcon = java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));
				Taskbar awtTaskbar = Taskbar.getTaskbar();
				awtTaskbar.setIconImage(awtIcon);
			} catch (final Throwable e) {
				log.warn("The os does not support setting taskbar icon");
			}
		}, "milkman-set-taskbar-icon");
		t.setDaemon(true);
		t.start();
	}

	public void switchToTheme(UiThemePlugin theme, boolean disableAnimations) {

		mainScene.getStylesheets().clear();
//		mainScene.getStylesheets().add(getClass().getResource(theme.getMainCss()).toExternalForm());
//		mainScene.getStylesheets().add(getClass().getResource(theme.getCodeCss()).toExternalForm());

		mainScene.getStylesheets().add(theme.getMainCss());
		mainScene.getStylesheets().add(theme.getCodeCss());

		if (disableAnimations){
			mainScene.getStylesheets().add("/themes/support/disable-animations.css");
		}
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
