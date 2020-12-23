package milkman.ui.main.themes;

import milkman.ui.plugin.UiThemePlugin;

public class TrumpDarkThemePlugin implements UiThemePlugin {

	@Override
	public String getName() {
		return "Trump Dark";
	}

	@Override
	public String getMainCss() {
		return "/themes/trump-dark.css";
	}

	@Override
	public String getCodeCss() {
		return "/themes/syntax/milkman-dark-syntax.css";
	}

}
