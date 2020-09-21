package milkman.plugin.note;


import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.assertions.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import milkman.domain.RequestContainer;

@Tag("ui")
@ExtendWith(ApplicationExtension.class)
public class NotesAspectEditorTest {

	private NotesAspect noteAspect;

	@Start
	public void setupStage(Stage stage) {
		NotesAspectEditor sut = new NotesAspectEditor();
		noteAspect = new NotesAspect();
		noteAspect.setNote("first");
		RequestContainer request = new RequestContainer() {

			@Override
			public String getType() {
				return "";
			}};
		request.addAspect(noteAspect);
		Tab root = sut.getRoot(request);
		stage.setScene(new Scene(new TabPane(root)));
		stage.show();
	}
	
	
	@Test
	void testBindingsWorkingAfterGC(FxRobot robot) {
		System.gc();
		TextInputControl noteTextArea = robot.lookup(".text-area").queryTextInputControl();
		assertThat(noteTextArea).hasText("first");
		noteTextArea.setText("second");
		
		assertThat(noteAspect.getNote()).isEqualTo("second");
		assertThat(noteAspect.isDirty()).isTrue();
	}

}
