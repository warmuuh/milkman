package milkman.ui.main;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import milkman.domain.Collection;
import milkman.ui.main.RequestCollectionComponent.RequestCollectionComponentFxml;
import milkman.utils.fxml.FxmlUtil;

@ExtendWith(ApplicationExtension.class)
class RequestCollectionComponentTest {
	
	
	private RequestCollectionComponent requests;


	@Start
	public void setupStage(Stage stage) {
		requests = new RequestCollectionComponent();
		
		Node root = new RequestCollectionComponentFxml(requests);
		stage.setScene(new Scene(new Pane(root)));
		
		stage.show();
	}
	
	
	@Test
	void testBindingsWorkingAfterGC(FxRobot robot) throws InterruptedException {
		requests.display(Arrays.asList(
					new Collection("1", "B request", false, Collections.emptyList(), Collections.emptyList()), // last
					new Collection("2", "A request", false, Collections.emptyList(), Collections.emptyList()), // second because 'A'
					new Collection("3", "C request", true, Collections.emptyList(), Collections.emptyList()) // first because starred
				));
		
		Thread.sleep(100);
		Set<Node> allCells = robot.lookup("#collectionContainer .tree-cell .label").queryAll();

		assertThat(allCells)
			.extracting(n -> ((Label)n).getText())
			.containsExactly("C request", "A request", "B request");
	}

	
	@Test
	void shouldExpandItemsOnClick(FxRobot robot) throws InterruptedException {
		requests.display(Arrays.asList(
				new Collection("2", "A request", false, Collections.emptyList(), Collections.emptyList())
				//new Collection("1", "A request", false, Collections.singletonList(new TestRequestContainer("test1")))
				));

		Thread.sleep(100);
		Set<Node> allCells = robot.lookup("#collectionContainer .tree-cell .label").queryAll();

		assertThat(allCells)
			.extracting(n -> ((Label)n).getText())
			.containsExactly("A request");
	}
}
