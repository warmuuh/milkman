package milkman.plugin.scripting;

import milkman.domain.Environment;
import milkman.domain.RequestExecutionContext;
import milkman.plugin.scripting.ScriptExecutor.ExecutionResult;
import milkman.ui.plugin.ActiveEnvironmentAware;
import milkman.ui.plugin.TemplateParameterResolverPlugin;

import java.util.List;
import java.util.Optional;

public class ScriptTemplateParameterResolverPlugin implements TemplateParameterResolverPlugin, ActiveEnvironmentAware {
    private Optional<Environment> activeEnvironment;

    @Override
    public String getPrefix() {
        return "js";
    }

    @Override
    public String lookupValue(String input) {
        ExecutionResult executionResult = ScriptingAspectPlugin.getGlobalExecutor().executeScript(input, null, null, new RequestExecutionContext(activeEnvironment, List.of()));
        return executionResult.getResult().orElse("").toString();
    }

    @Override
    public void setActiveEnvironment(Optional<Environment> activeEnvironment) {
        this.activeEnvironment = activeEnvironment;
    }
}
