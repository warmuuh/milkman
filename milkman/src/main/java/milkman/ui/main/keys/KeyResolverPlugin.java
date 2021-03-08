package milkman.ui.main.keys;

import lombok.Setter;
import milkman.domain.KeySet;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ActiveKeySetAware;
import milkman.ui.plugin.TemplateParameterResolverPlugin;
import milkman.ui.plugin.ToasterAware;

public class KeyResolverPlugin implements TemplateParameterResolverPlugin, ActiveKeySetAware, ToasterAware {

    @Setter
    private KeySet activeKeySet;
    @Setter
    private Toaster toaster;

    @Override
    public String lookupValue(String input) {
        try {
            return activeKeySet.getValueForKey(input)
                    .orElse("{{key:" + input + "}}");
        } catch (Exception e) {
            toaster.showToast("Failed to resolve key:" + input + ": " + e.getMessage());
            return "{{key:" + input + "}}";
        }
    }

    @Override
    public String getPrefix() {
        return "key";
    }

}
