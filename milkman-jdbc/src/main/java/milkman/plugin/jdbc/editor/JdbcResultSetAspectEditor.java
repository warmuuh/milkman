package milkman.plugin.jdbc.editor;

import java.util.List;

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
		editor.disableEdition();
		
		for(int i = 0; i < rowSetAspect.getColumnNames().size(); ++i) {
			editor.addColumn(rowSetAspect.getColumnNames().get(i), getRowValue(i), (r, v) -> {});	
		}
		
		editor.setItems(rowSetAspect.getRows());
		
		return new Tab("Response Headers", editor);
	}

	
	private Function1<List<Object>, String> getRowValue(int columnIdx) {
		return row -> row.get(columnIdx).toString();
	}
	
	
	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(RowSetResponseAspect.class).isPresent();
	}

}
