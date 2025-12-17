package milkman.plugin.mcp.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpEventsAspect;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.ui.plugin.ResponseAspectEditor;

public class McpEventAspectEditor implements ResponseAspectEditor {

	private TextArea editor;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new TextArea("Listening for events...");
		VBox.setVgrow(editor, Priority.ALWAYS);
		McpEventsAspect rowSetAspect = response.getAspect(McpEventsAspect.class).get();

		initEditor(rowSetAspect);

		return new Tab("Events", editor);
	}


	private void initEditor(McpEventsAspect rowSetAspect) {
		rowSetAspect.getEvents().subscribe(s -> {
			Platform.runLater(() -> {
				editor.appendText("\n"+s);
				editor.positionCaret(editor.getText().length());
			});
		});
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(McpEventsAspect.class).isPresent();
	}

}
