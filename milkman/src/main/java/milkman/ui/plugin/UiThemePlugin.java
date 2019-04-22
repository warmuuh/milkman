package milkman.ui.plugin;

/**
* extension point for adding new themes
*/
public interface UiThemePlugin {
    /**
    * name of the theme. will be shown in the Ui.
    */
	String getName();

    /**
    * path of the CSS file (in classpath) for the application theme
    */
	String getMainCss();

    /**
    * path of the CSS file (in classpath) for the content editor theme
    */
    String getCodeCss();
}
