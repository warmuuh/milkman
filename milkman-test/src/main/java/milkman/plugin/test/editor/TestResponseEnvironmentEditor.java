package milkman.plugin.test.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.TestResultAspect;
import milkman.plugin.test.domain.TestResultEnvAspect;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.ResponseAspectEditor;

import java.util.Comparator;

import static milkman.domain.Environment.EnvironmentEntry;

public class TestResponseEnvironmentEditor implements ResponseAspectEditor {


    @Override
    public Tab getRoot(RequestContainer request, ResponseContainer response) {
        TestResultEnvAspect testAspect = response.getAspect(TestResultEnvAspect.class).get();
        TableEditor<EnvironmentEntry> editor = new TableEditor<>("tests.list");
        editor.addReadOnlyColumn("name", EnvironmentEntry::getName);
        editor.addReadOnlyColumn("value", EnvironmentEntry::getValue);

        TestResultAspect testResultAspect = response.getAspect(TestResultAspect.class).get();
        testResultAspect.getResults().subscribe((n) -> Platform.runLater(() -> {
                editor.setItems(testAspect.getEnvironment().getEntries(), Comparator.comparing(EnvironmentEntry::getName));
        }));

        editor.setRowToStringConverter(e -> e.getName() + ": " + e.getValue());

        return new Tab("Result Environment", editor);
    }

    @Override
    public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
        return response.getAspect(TestResultEnvAspect.class).isPresent();
    }
}
