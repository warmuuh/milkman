package milkman.ui.main;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import milkman.PlatformUtil;
import milkman.ui.commands.AppCommand;
import milkman.ui.commands.AppCommand.EditCurrentEnvironment;
import milkman.ui.commands.UiCommand;
import milkman.ui.commands.UiCommand.*;
import milkman.utils.Event;

import javax.inject.Singleton;

@Singleton
public class HotkeyManager {

	public final Event<AppCommand> onAppCommand = new Event<AppCommand>();
	public final Event<UiCommand> onCommand = new Event<UiCommand>();
	
	public void registerGlobalHotkeys(Scene scene) {
		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.ENTER),
				() -> onCommand.invoke(new SubmitActiveRequest()));

		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.N),
				() -> onCommand.invoke(new NewRequest(null)));

		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.W),
				() -> onCommand.invoke(new CloseActiveRequest()));

		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.R),
				() -> onCommand.invoke(new RenameActiveRequest()));
		
		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.S),
				() -> onCommand.invoke(new SaveActiveRequest()));
		

		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.E),
				() -> onAppCommand.invoke(new EditCurrentEnvironment()));


		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE),
				() -> {
					onCommand.invoke(new CancelActiveRequest());
					onCommand.invoke(new CancelHighlight());
				});


		scene.getAccelerators().put(PlatformUtil.getControlKeyCombination(KeyCode.SPACE),
				() -> onCommand.invoke(new HighlightVariables()));


	}



}
