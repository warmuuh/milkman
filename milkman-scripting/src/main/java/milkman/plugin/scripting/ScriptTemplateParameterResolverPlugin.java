package milkman.plugin.scripting;

import milkman.domain.RequestExecutionContext;
import milkman.ui.plugin.TemplateParameterResolverPlugin;

import java.util.List;
import java.util.Optional;

public class ScriptTemplateParameterResolverPlugin implements TemplateParameterResolverPlugin {
    @Override
    public String getPrefix() {
        return "js";
    }

    @Override
    public String lookupValue(String input) {
        ScriptExecutor.ExecutionResult executionResult = ScriptingAspectPlugin.getGlobalExecutor().executeScript(input, null, null, new RequestExecutionContext(Optional.empty(), List.of()));
        return executionResult.getResult().orElse("").toString();
    }
}
