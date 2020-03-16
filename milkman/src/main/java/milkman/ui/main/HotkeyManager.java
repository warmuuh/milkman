package milkman.ui.main;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), 
				() -> onCommand.invoke(new SubmitActiveRequest()));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), 
				() -> onCommand.invoke(new NewRequest(null)));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN), 
				() -> onCommand.invoke(new CloseActiveRequest()));

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), 
				() -> onCommand.invoke(new RenameActiveRequest()));
		
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), 
				() -> onCommand.invoke(new SaveActiveRequest()));
		

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), 
				() -> onAppCommand.invoke(new EditCurrentEnvironment()));
		

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), 
				() -> onCommand.invoke(new CancelActiveRequest()));


		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN),
				() -> onCommand.invoke(new HighlightVariables()));


	}



}
