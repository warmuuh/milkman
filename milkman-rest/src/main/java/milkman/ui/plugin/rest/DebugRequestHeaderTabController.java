package milkman.ui.plugin.rest;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.DebugRequestHeaderAspect;
import milkman.ui.plugin.rest.domain.HeaderEntry;

import static milkman.utils.fxml.FxmlBuilder.icon;

public class DebugRequestHeaderTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		DebugRequestHeaderAspect headers = response.getAspect(DebugRequestHeaderAspect.class).get();
		JfxTableEditor<HeaderEntry> editor = new JfxTableEditor<HeaderEntry>("rest.debug.headers.list");
		editor.disableAddition();
		editor.addReadOnlyColumn("Name", HeaderEntry::getName);
		editor.addReadOnlyColumn("Value", HeaderEntry::getValue);
		editor.setRowToStringConverter(this::headerToString);
		
		editor.setItems(headers.getEntries());
		Tab tab = new Tab("Request Headers",  editor);
		tab.setGraphic(icon(FontAwesomeIcon.BUG, "1em"));
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(DebugRequestHeaderAspect.class).isPresent();
	}

	private String headerToString(HeaderEntry header) {
		return header.getName() + ": " + header.getValue();
	}
}
