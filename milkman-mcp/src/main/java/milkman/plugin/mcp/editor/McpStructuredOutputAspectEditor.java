package milkman.plugin.mcp.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpStructuredOutputAspect;
import milkman.ui.plugin.ResponseAspectEditor;

public class McpStructuredOutputAspectEditor implements ResponseAspectEditor {

	private TextArea editor;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new TextArea("pending...");
		VBox.setVgrow(editor, Priority.ALWAYS);
		McpStructuredOutputAspect rowSetAspect = response.getAspect(McpStructuredOutputAspect.class).get();

		initEditor(rowSetAspect);

		return new Tab("Structured Output", editor);
	}


	private void initEditor(McpStructuredOutputAspect rowSetAspect) {
		rowSetAspect.getStructuredOutput().subscribe(s -> {
			Platform.runLater(() -> editor.setText(s));
		});
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(McpStructuredOutputAspect.class).isPresent();
	}

}
