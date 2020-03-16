package milkman.ctrl;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.domain.Environment;
import org.reactfx.value.Var;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class VariableResolver {


    private Optional<Environment> activeEnvironment;
    private List<Environment> globalEnvironments;

    public VariableResolver(Optional<Environment> activeEnvironment, List<Environment> globalEnvironments) {
        this.activeEnvironment = activeEnvironment;
        this.globalEnvironments = globalEnvironments;
    }

    public VariableData getVariableData(String variableName){
        List<Environment> allEnvs = new LinkedList<>(globalEnvironments);
        activeEnvironment.ifPresent(e -> allEnvs.add(0, e));

        for (Environment env : allEnvs) {
            var foundEntry = env.getEntries().stream()
                        .filter(entry -> entry.getName().equals(variableName))
                        .findFirst();
            if (foundEntry.isPresent()){
                return new VariableData(variableName, foundEntry.get().getValue(), env, false);
            }
        }

        return new VariableData(variableName, "", activeEnvironment.orElse(null), true);
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
