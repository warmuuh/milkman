package milkman.ui.main.keys;

import milkman.domain.KeySet;
import milkman.ui.plugin.ActiveKeySetAware;
import milkman.ui.plugin.TemplateParameterResolverPlugin;

public class KeyResolverPlugin implements TemplateParameterResolverPlugin, ActiveKeySetAware {

    private KeySet keySet;

    @Override
    public String lookupValue(String input) {
        return keySet.getValueForKey(input)
                .orElseThrow(() -> new RuntimeException("Failed to resolve key: " + input));
    }

    @Override
    public String getPrefix() {
        return "key";
    }

    @Override
    public void setActiveKeySet(KeySet keySet) {
        this.keySet = keySet;
    }
}
