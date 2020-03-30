package milkman.plugin.scripting;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import reactor.util.annotation.Nullable;

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
    String executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context);

}
