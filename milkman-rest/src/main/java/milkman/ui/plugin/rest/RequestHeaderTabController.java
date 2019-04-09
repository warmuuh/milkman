package milkman.ui.plugin.rest;

import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.fxml.FxmlUtil;

import static milkman.utils.FunctionalUtils.*;

public class RequestHeaderTabController implements RequestAspectEditor {


	@Override
	@SneakyThrows
	public Tab getRoot(RequestAspect aspect) {
		RestHeaderAspect headers = (RestHeaderAspect) aspect;
		JfxTableEditor<HeaderEntry> editor = FxmlUtil.loadAndInitialize("/components/TableEditor.fxml");
		editor.setEditable(true);
		editor.addCheckboxColumn("Enabled", HeaderEntry::isEnabled, run(HeaderEntry::setEnabled).andThen(() -> aspect.setDirty(true)));
		editor.addColumn("Name", HeaderEntry::getName, run(HeaderEntry::setName).andThen(() -> aspect.setDirty(true)));
		editor.addColumn("Value", HeaderEntry::getValue,run(HeaderEntry::setValue).andThen(() -> aspect.setDirty(true)));
		editor.addDeleteColumn("Delete", () -> aspect.setDirty(true));
		
		editor.setItems(headers.getEntries(), () -> {
			aspect.setDirty(true);
			return new HeaderEntry("", "", true);
		});
		return new Tab("Headers", editor);
	}

	@Override
	public boolean canHandleAspect(RequestAspect aspect) {
		return aspect instanceof RestHeaderAspect;
	}

}
