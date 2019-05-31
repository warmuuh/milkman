package milkman.ui.plugin.rest;

import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.FunctionalUtils.*;

import java.util.UUID;

public class RequestHeaderTabController implements RequestAspectEditor {


	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		RestHeaderAspect headers = request.getAspect(RestHeaderAspect.class).get();
		JfxTableEditor<HeaderEntry> editor = FxmlUtil.loadAndInitialize("/components/TableEditor.fxml");
		editor.enableAddition(() -> {
			headers.setDirty(true);
			return new HeaderEntry(UUID.randomUUID().toString(), "", "", true);
		});
		editor.addCheckboxColumn("Enabled", HeaderEntry::isEnabled, run(HeaderEntry::setEnabled).andThen(() -> headers.setDirty(true)));
		editor.addColumn("Name", HeaderEntry::getName, run(HeaderEntry::setName).andThen(() -> headers.setDirty(true)));
		editor.addColumn("Value", HeaderEntry::getValue,run(HeaderEntry::setValue).andThen(() -> headers.setDirty(true)));
		editor.addDeleteColumn("Delete", () -> headers.setDirty(true));
		
		editor.setRowToStringConverter(this::headerToString);
		
		editor.setItems(headers.getEntries());
		return new Tab("Headers", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(RestHeaderAspect.class).isPresent();
	}
	
	private String headerToString(HeaderEntry header) {
		String prefix = "";
		if (!header.isEnabled())
			prefix = "//";
		return prefix + header.getName() + ": " + header.getValue();
	}

}
