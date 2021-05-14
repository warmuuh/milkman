package milkman.templater;

import lombok.RequiredArgsConstructor;
import milkman.ui.plugin.TemplateParameterResolverPlugin;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PrefixedTemplaterResolver {

    private final List<TemplateParameterResolverPlugin> templaters;

    public Optional<String> resolveViaPluginTemplater(String input) {
        int idx = input.indexOf(':');
        if (idx < 0 || input.length() <= idx + 1){
            return Optional.empty();
        }

        String prefix = input.substring(0, idx);
        String value = input.substring(idx+1);

        return templaters.stream()
                .filter(t -> t.getPrefix().equals(prefix))
                .map(t -> t.lookupValue(value))
                .findAny();
    }

}
