package milkman.ctrl;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.domain.Environment;
import milkman.templater.PrefixedTemplaterResolver;
import milkman.ui.plugin.Templater;
import org.reactfx.value.Var;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class VariableResolver {


    private Optional<Environment> activeEnvironment;
    private List<Environment> globalEnvironments;
    private PrefixedTemplaterResolver resolver;

    public VariableResolver(Optional<Environment> activeEnvironment, List<Environment> globalEnvironments, PrefixedTemplaterResolver resolver) {
        this.activeEnvironment = activeEnvironment;
        this.globalEnvironments = globalEnvironments;
        this.resolver = resolver;
    }

    public VariableData getVariableData(String variableName){
        List<Environment> allEnvs = new LinkedList<>(globalEnvironments);
        activeEnvironment.ifPresent(e -> allEnvs.add(0, e));

        //step1: lookup in envs:
        for (Environment env : allEnvs) {
            var foundEntry = env.getEntries().stream()
                        .filter(entry -> entry.getName().equals(variableName))
                        .findFirst();
            if (foundEntry.isPresent()){
                return new VariableData(variableName, foundEntry.get().getValue(), env, false);
            }
        }
        //step2: try to resolve via templater or return "new variable"
        return resolver.resolveViaPluginTemplater(variableName)
                .map(value -> new VariableData(variableName, value, null, false))
                .orElseGet(() ->  new VariableData(variableName, "", activeEnvironment.orElse(null), true));
    }

    @RequiredArgsConstructor
    public static class VariableData {
        @Getter
        private final String name;

        @Getter
        private final String value;

        private final Environment environment;

        @Getter
        private final boolean newVariable;

        public String getEnvironmentName(){
            if (environment != null) {
                return environment.getName();
            }
            return "<no environment set>";
        }

        public boolean isWriteable(){
            return  environment != null;
        }

        public void writeNewValue(String newValue){
            if (environment != null){
                environment.setOrAdd(name, newValue);
            }
        }
    }
}
