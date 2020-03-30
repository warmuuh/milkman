package milkman.plugin.scripting.graaljs;

import milkman.domain.RequestExecutionContext;
import milkman.ui.main.Toaster;
import org.junit.jupiter.api.Test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GraalJsTest {

    public static final String SOURCE = "console.log(\"test\");";


    @Test
    void shouldExecJs() throws IOException {
        var executor = new GraaljsExecutor(mock(Toaster.class));
        var output = executor.executeScript(SOURCE, null, null, new RequestExecutionContext(Optional.empty()));

        assertThat(output).isEqualTo("test\n");
    }
}
