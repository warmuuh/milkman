package milkman.ui.plugin.rest;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

public class ResponseHeaderTabController implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		RestResponseHeaderAspect headers = response.getAspect(RestResponseHeaderAspect.class).get();
		JfxTableEditor<HeaderEntry> editor = new JfxTableEditor<HeaderEntry>();
		editor.disableAddition();
		editor.addReadOnlyColumn("Name", HeaderEntry::getName);
		editor.addReadOnlyColumn("Value", HeaderEntry::getValue);
		editor.setRowToStringConverter(this::headerToString);
		
		editor.setItems(headers.getEntries());
		
		return new Tab("Response Headers", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(RestResponseHeaderAspect.class).isPresent();
	}

	private String headerToString(HeaderEntry header) {
		return header.getName() + ": " + header.getValue();
	}
}
