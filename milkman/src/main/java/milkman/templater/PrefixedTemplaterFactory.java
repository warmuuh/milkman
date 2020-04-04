package milkman.templater;

import lombok.RequiredArgsConstructor;
import milkman.domain.Environment;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.UiPluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class PrefixedTemplaterFactory {

    private final UiPluginManager pluginManager;

    public Templater createTemplater(Optional<Environment> activeEnvironment, List<Environment> globalEnvironments){
        return new EnvironmentTemplater(activeEnvironment, globalEnvironments, new PrefixedTemplaterResolver(pluginManager.loadTemplaterPlugins()));
    }

}
