package milkman.ui.main;

import lombok.RequiredArgsConstructor;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.UiPluginManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class ThemeSwitcher {

	private final UiPluginManager plugins;
	private final MainWindow mainWindow;

	
	public List<String> getThemes(){
		return plugins.loadThemePlugins().stream()
			.map(p -> p.getName())
			.collect(Collectors.toList());
	}
	
	
	
	public void setTheme(String themeName, boolean disableAnimations){
		plugins.loadThemePlugins().stream()
			.filter(p -> p.getName().equals(themeName))
			.findAny().ifPresent(theme -> mainWindow.switchToTheme(theme, disableAnimations));
	}
	
	
	@PostConstruct
	public void setup() {
		CoreApplicationOptionsProvider.setThemeSwitcher(this);
	}
}
