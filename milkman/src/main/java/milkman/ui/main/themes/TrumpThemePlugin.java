package milkman.ui.main.themes;

import milkman.ui.plugin.UiThemePlugin;

public class TrumpThemePlugin implements UiThemePlugin {

	@Override
	public String getName() {
		return "Trump";
	}

	@Override
	public String getMainCss() {
		return "/themes/trump.css";
	}

	@Override
	public String getCodeCss() {
		return "/themes/syntax/milkman-syntax.css";
	}

}
