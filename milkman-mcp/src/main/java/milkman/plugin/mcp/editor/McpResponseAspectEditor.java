package milkman.plugin.mcp.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.mcp.domain.McpResponseAspect;
import milkman.ui.plugin.ResponseAspectEditor;

public class McpResponseAspectEditor implements ResponseAspectEditor {

	private TextArea editor;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new TextArea("mcp.result");
		VBox.setVgrow(editor, Priority.ALWAYS);
		McpResponseAspect rowSetAspect = response.getAspect(McpResponseAspect.class).get();

		initEditor(rowSetAspect);

		return new Tab("Contents", editor);
	}


	private void initEditor(McpResponseAspect rowSetAspect) {
		rowSetAspect.getResponse().subscribe(s -> {
			Platform.runLater(() -> editor.setText(s));
		});
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(McpResponseAspect.class).isPresent();
	}

}
