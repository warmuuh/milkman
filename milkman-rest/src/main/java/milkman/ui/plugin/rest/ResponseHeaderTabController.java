package milkman.ui.plugin.rest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkman.utils.fxml.FxmlUtil;

public class ResponseHeaderTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		RestResponseHeaderAspect headers = response.getAspect(RestResponseHeaderAspect.class).get();
		JfxTableEditor<HeaderEntry> editor = FxmlUtil.loadAndInitialize("/components/TableEditor.fxml");
		editor.disableEdition();
		editor.addColumn("Name", HeaderEntry::getName, HeaderEntry::setName);
		editor.addColumn("Value", HeaderEntry::getValue, HeaderEntry::setValue);
		
		editor.setItems(headers.getEntries());
		
		return new Tab("Response Headers", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(RestResponseHeaderAspect.class).isPresent();
	}

	@Override
	public int getOrder() {
		return 200;
	}
}
