package milkman.plugin.scripting.graaljs;

import milkman.domain.RequestExecutionContext;
import milkman.ui.main.Toaster;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GraalJsTest {

    public static final String SOURCE = "console.log(\"test\");";


    @Test
    void shouldExecJs() throws IOException {
        var executor = new GraaljsExecutor(null);
        var output = executor.executeScript(SOURCE, null, null, new RequestExecutionContext(Optional.empty(), List.of())).getConsoleOutput();

        assertThat(output).isEqualTo("test\n");
    }
}
