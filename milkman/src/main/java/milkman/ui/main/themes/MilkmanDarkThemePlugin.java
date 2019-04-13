package milkman.ui.main.themes;

import milkman.ui.plugin.UiThemePlugin;

public class MilkmanDarkThemePlugin implements UiThemePlugin {

	@Override
	public String getName() {
		return "Milkman Dark";
	}

	@Override
	public String getMainCss() {
		return "/themes/milkman-dark.css";
	}

	@Override
	public String getCodeCss() {
		return "/themes/syntax/milkman-dark-syntax.css";
	}

}
