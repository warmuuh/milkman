package milkman.ui.plugin;

/**
* a templater that modifies input-strings in an
* implementation-specific way, e.g. replaces environment-varables.
*/
public interface TemplateParameterResolverPlugin {

    /**
     * returns a string with all replaced tags
     */
    public String lookupValue(String input);

    /**
     * the prefix under which the templater is registered
     * @return
     */
    public String getPrefix();

}
