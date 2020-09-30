package milkman.plugin.scripting;

import lombok.Value;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;

import java.util.Optional;

public interface ScriptExecutor {

    /**
     * executes a script and returns the output of the script
     *
     * @param source
     * @param request
     * @param response nullable response object
     * @param context
     * @return output of the script (via console.log  / print ...)
     */
    ExecutionResult executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context);

    @Value
    class ExecutionResult {
        String consoleOutput;
        Optional<Object> result;
        Optional<Throwable> error;
    }
}
