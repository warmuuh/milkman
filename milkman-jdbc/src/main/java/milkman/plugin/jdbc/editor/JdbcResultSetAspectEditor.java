package milkman.plugin.jdbc.editor;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import io.vavr.Function1;
import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.utils.fxml.FxmlUtil;

public class JdbcResultSetAspectEditor implements ResponseAspectEditor {

	@Override
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		RowSetResponseAspect rowSetAspect = response.getAspect(RowSetResponseAspect.class).get();
		JfxTableEditor<List<Object>> editor = FxmlUtil.loadAndInitialize("/components/TableEditor.fxml");
		editor.disableAddition();
		
		for(int i = 0; i < rowSetAspect.getColumnNames().size(); ++i) {
			editor.addReadOnlyColumn(rowSetAspect.getColumnNames().get(i), getRowValue(i));	
		}
		
		editor.setItems(rowSetAspect.getRows());
		editor.setRowToStringConverter(this::rowToString);
		return new Tab("Result", editor);
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
	
	private String rowToString(List<Object> row) {
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
