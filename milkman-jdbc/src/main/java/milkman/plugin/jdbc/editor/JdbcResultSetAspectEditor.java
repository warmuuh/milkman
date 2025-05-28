package milkman.plugin.jdbc.editor;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.ResponseAspectEditor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class JdbcResultSetAspectEditor implements ResponseAspectEditor {

	private TableEditor<List<String>> editor;

	private boolean contentIsTransposed;

	private RowSetResponseAspect transposedContent;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new TableEditor<>("jdbc.result.list");
		editor.disableAddition();
		editor.setRowToStringConverter(this::rowToString);
		VBox.setVgrow(editor, Priority.ALWAYS);
		RowSetResponseAspect rowSetAspect = response.getAspect(RowSetResponseAspect.class).get();

		initEditor(rowSetAspect);
		
		HBox tableToolbar = setupToolbar(rowSetAspect);
		VBox vBox = new VBox(tableToolbar, editor);

		return new Tab("Result", vBox);
	}


	private void initEditor(RowSetResponseAspect rowSetAspect) {
		for(int i = 0; i < rowSetAspect.getColumnNames().size(); ++i) {
			editor.addReadOnlyColumn(rowSetAspect.getColumnNames().get(i), getRowValue(i));	
		}
		editor.setItems(rowSetAspect.getRows());
	}


	private HBox setupToolbar(RowSetResponseAspect rowSetAspect) {
		JFXButton copyResultBtn = new JFXButton("Copy to Clipboard");
		copyResultBtn.setOnAction(e -> copyResultToClipboard(rowSetAspect));
		
		JFXButton transposeBtn = new JFXButton("Transpose");
		transposeBtn.setOnAction(e -> transpose(rowSetAspect));
		
		
		HBox tableToolbar = new HBox(copyResultBtn, transposeBtn);
		tableToolbar.getStyleClass().add("response-header");
		
		return tableToolbar;
	}

	
	private void transpose(RowSetResponseAspect rowSetAspect) {
		editor.clearContent();
		if (contentIsTransposed) {
			initEditor(rowSetAspect);
		} else {
			if (transposedContent == null) {
				transposedContent = createTransposedContent(rowSetAspect);
			}
			initEditor(transposedContent);
		}
		contentIsTransposed = !contentIsTransposed;
	}


	private RowSetResponseAspect createTransposedContent(RowSetResponseAspect rowSetAspect) {
		List<String> columnNames = new LinkedList<>();
		List<List<String>> rows = new LinkedList<>();
		
		columnNames.add("Key");
		for(int idx = 1; idx <= rowSetAspect.getRows().size(); ++idx) {
			columnNames.add("item " + idx);
		}
		
		for(int curRow = 0; curRow < rowSetAspect.getColumnNames().size(); ++curRow) {
			LinkedList<String> curRowL = new LinkedList<>();
			rows.add(curRowL);
			curRowL.add(rowSetAspect.getColumnNames().get(curRow));
			for(int curCol = 0; curCol < rowSetAspect.getRows().size(); ++curCol) {
				curRowL.add(rowSetAspect.getRows().get(curCol).get(curRow));
			}
		}
		RowSetResponseAspect result = new RowSetResponseAspect();
		result.setColumnNames(columnNames);
		result.setRows(rows);
		return result;
	}


	private void copyResultToClipboard(RowSetResponseAspect rowSetAspect) {
		RowSetResponseAspect source = contentIsTransposed ? transposedContent : rowSetAspect;
		StringBuilder b = new StringBuilder();
		b.append(rowToString(source.getColumnNames()));
		for (List<String> row : source.getRows()) {
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
		return response.getAspect(RowSetResponseAspect.class).isPresent();
	}

}
