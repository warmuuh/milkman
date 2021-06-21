package milkman.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import lombok.RequiredArgsConstructor;
import milkman.ui.components.JfxTableEditor.TextFieldEditorBuilderPatch;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class InitializingCellBuilder extends TextFieldEditorBuilderPatch {

	private final Consumer<TextField> initializer;
	
	@Override
	public Region createNode(String value, EventHandler<KeyEvent> keyEventsHandler,
			ChangeListener<Boolean> focusChangeListener) {
		Region node = super.createNode(value, keyEventsHandler, focusChangeListener);
		initializer.accept(textField);
		return node;
	}
}
