package milkman.ui.components;

import java.util.function.Consumer;

import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InitializingCellBuilder extends TextFieldEditorBuilder {

	private final Consumer<TextField> initializer;
	
	@Override
	public Region createNode(String value, EventHandler<KeyEvent> keyEventsHandler,
			ChangeListener<Boolean> focusChangeListener) {
		Region node = super.createNode(value, keyEventsHandler, focusChangeListener);
		initializer.accept(textField);
		return node;
	}
}
