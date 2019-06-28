package milkman.plugin.jdbc.editor;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.jfoenix.controls.JFXButton;

import io.vavr.Function1;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.utils.fxml.FxmlUtil;

public class JdbcResultSetAspectEditor implements ResponseAspectEditor {

	private JfxTableEditor<List<Object>> editor;

	private boolean contentIsTransposed = false;

	private RowSetResponseAspect transposedContent;

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		editor = new JfxTableEditor<List<Object>>();
		editor.disableAddition();
		editor.setRowToStringConverter(this::rowToString);
		
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
		copyResultBtn.setOnAction(e -> this.copyResultToClipboard(rowSetAspect));
		
		JFXButton transposeBtn = new JFXButton("Transpose");
		transposeBtn.setOnAction(e -> this.transpose(rowSetAspect));
		
		
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
		List<String> columnNames = new LinkedList<String>();
		List<List<Object>> rows = new LinkedList<List<Object>>();
		
		columnNames.add("Key");
		for(int idx = 1; idx <= rowSetAspect.getRows().size(); ++idx) {
			columnNames.add("item " + idx);
		}
		
		for(int curRow = 0; curRow < rowSetAspect.getColumnNames().size(); ++curRow) {
			LinkedList<Object> curRowL = new LinkedList<Object>();
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
		for (List<Object> row : source.getRows()) {
			b.append(System.lineSeparator());
			b.append(rowToString(row));
		}
		final ClipboardContent clipboardContent = new ClipboardContent();
	    clipboardContent.putString(b.toString());
	    Clipboard.getSystemClipboard().setContent(clipboardContent);
	}


	private Function1<List<Object>, String> getRowValue(int columnIdx) {
		return row -> {
			Object value = row.get(columnIdx);
			String stringValue = valueToString(value);
			return stringValue;
		};
	}


	private String valueToString(Object value) {
		if (value instanceof Blob) {
			try {
				value = IOUtils.toString(((Blob) value).getBinaryStream());
			} catch (IOException | SQLException e) {
				value = "BLOB";
			}
		}
		String stringValue = value != null ? value.toString() : "NULL";
		return stringValue;
	}
	
	private String rowToString(List<? extends Object> row) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Object value : row) {
			if (!first)
				b.append("\t");
			first = false;
			b.append(valueToString(value));
		}
		return b.toString();
	}
	
	
	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(RowSetResponseAspect.class).isPresent();
	}

}
