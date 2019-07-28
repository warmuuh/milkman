package milkman.ui.plugin.rest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

import static org.testfx.assertions.api.Assertions.*;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class RestRequestEditControllerTest {

	
	private RestRequestContainer request;


	@Start
	public void setupStage(Stage stage) {
		RestRequestEditController sut = new RestRequestEditController();
		
		sut.setAutoCompleter(new AutoCompleter(() -> Collections.emptyList()));
		Node root = sut.getRoot();
		stage.setScene(new Scene(new Pane(root)));

		request = new RestRequestContainer("name", "url", "GET");
		sut.displayRequest(request);
		stage.show();
	}
	
	
	@Test
	void testBindingsWorkingAfterGC(FxRobot robot) {
		System.gc();
		TextInputControl requestUrlInput = robot.lookup("#requestUrl").queryTextInputControl();
		assertThat(requestUrlInput).hasText("url");
		requestUrlInput.setText("newUrl");
		
		assertThat(request.getUrl()).isEqualTo("newUrl");
		assertThat(request.isDirty()).isTrue();
		
	}

}
