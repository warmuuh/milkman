package milkman.plugin.mcp.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpInstructionsAspect;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.ui.plugin.ResponseAspectEditor;

public class McpInstructionsAspectEditor implements ResponseAspectEditor {

	private TextArea editor;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new TextArea("instructions");
		VBox.setVgrow(editor, Priority.ALWAYS);
		McpInstructionsAspect rowSetAspect = response.getAspect(McpInstructionsAspect.class).get();

		initEditor(rowSetAspect);

		return new Tab("Instructions", editor);
	}


	private void initEditor(McpInstructionsAspect rowSetAspect) {
		rowSetAspect.getInstructions().subscribe(s -> {
			Platform.runLater(() -> editor.setText(s));
		});
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(McpResponseAspect.class).isPresent();
	}

}
