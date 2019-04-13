package milkman.ui.main.themes;

import milkman.ui.plugin.UiThemePlugin;

public class MilkmanThemePlugin implements UiThemePlugin {

	@Override
	public String getName() {
		return "Milkman";
	}

	@Override
	public String getMainCss() {
		return "/themes/milkman.css";
	}

	@Override
	public String getCodeCss() {
		return "/themes/syntax/milkman-syntax.css";
	}

}
