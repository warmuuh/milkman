package milkman.ui.plugin;

import java.util.List;

/**
* a templater that modifies input-strings in an
* implementation-specific way, e.g. replaces environment-varables.
*/
public interface TemplateParameterResolverPlugin {

    /**
     * returns a string with all replaced tags
     */
    String lookupValue(String input);

    /**
     * the prefix under which the templater is registered
     * @return
     */
    String getPrefix();

    default List<String> getAllEntries(){
        return List.of();
    }

}
