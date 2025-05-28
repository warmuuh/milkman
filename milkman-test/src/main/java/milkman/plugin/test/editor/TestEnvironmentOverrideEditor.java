package milkman.plugin.test.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.test.domain.TestAspect;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.RequestAspectEditor;

import java.util.Comparator;
import java.util.UUID;

import static milkman.domain.Environment.EnvironmentEntry;

public class TestEnvironmentOverrideEditor implements RequestAspectEditor {


	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		TestAspect testAspect = request.getAspect(TestAspect.class).get();
		TableEditor<EnvironmentEntry> editor = new TableEditor<>("tests.env.list");
		editor.enableAddition(() -> {
			Platform.runLater( () -> testAspect.setDirty(true));
			return new EnvironmentEntry(UUID.randomUUID().toString(), "", "", true);
		});
		editor.addCheckboxColumn("Enabled", EnvironmentEntry::isEnabled, EnvironmentEntry::setEnabled);
		editor.addColumn("name", EnvironmentEntry::getName, EnvironmentEntry::setName);
		editor.addColumn("value", EnvironmentEntry::getValue, EnvironmentEntry::setValue);
		editor.addDeleteColumn("delete");
		editor.setItems(testAspect.getEnvironmentOverride(), Comparator.comparing(EnvironmentEntry::getName));
		editor.setRowToStringConverter(e -> e.getName() + ": " + e.getValue());

		return new Tab("Environment Override", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(TestAspect.class).isPresent();
	}
	
}
