package milkman.plugin.nosql.editor;

import com.jfoenix.controls.JFXButton;
import java.util.List;
import java.util.function.Function;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.nosql.domain.NosqlResponseAspect;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.ResponseAspectEditor;

public class NosqlResponseAspectEditor implements ResponseAspectEditor {

	private TableEditor<List<String>> editor;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new TableEditor<>("nosql.result.list");
		editor.disableAddition();
		editor.setRowToStringConverter(this::rowToString);
		VBox.setVgrow(editor, Priority.ALWAYS);
		NosqlResponseAspect rowSetAspect = response.getAspect(NosqlResponseAspect.class).get();

		initEditor(rowSetAspect);

		HBox tableToolbar = setupToolbar(rowSetAspect);
		VBox vBox = new VBox(tableToolbar, editor);

		return new Tab("Result", vBox);
	}


	private void initEditor(NosqlResponseAspect rowSetAspect) {
		for(int i = 0; i < rowSetAspect.getColumnNames().size(); ++i) {
			editor.addReadOnlyColumn(rowSetAspect.getColumnNames().get(i), getRowValue(i));	
		}
		editor.setItems(rowSetAspect.getRows());
	}

	private HBox setupToolbar(NosqlResponseAspect nosqlResult) {
		JFXButton copyResultBtn = new JFXButton("Copy to Clipboard");
		copyResultBtn.setOnAction(e -> copyResultToClipboard(nosqlResult));

		HBox tableToolbar = new HBox(copyResultBtn);
		tableToolbar.getStyleClass().add("response-header");

		return tableToolbar;
	}

	private void copyResultToClipboard(NosqlResponseAspect rowSetAspect) {
		StringBuilder b = new StringBuilder();
		b.append(rowToString(rowSetAspect.getColumnNames()));
		for (List<String> row : rowSetAspect.getRows()) {
			b.append(System.lineSeparator());
			b.append(rowToString(row));
		}
		ClipboardContent clipboardContent = new ClipboardContent();
	    clipboardContent.putString(b.toString());
	    Clipboard.getSystemClipboard().setContent(clipboardContent);
	}


	private Function<List<String>, String> getRowValue(int columnIdx) {
		return row -> row.get(columnIdx);
	}


	private String rowToString(List<String> row) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Object value : row) {
			if (!first)
				b.append("\t");
			first = false;
			b.append(value);
		}
		return b.toString();
	}
	
	
	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(NosqlResponseAspect.class).isPresent();
	}

}
