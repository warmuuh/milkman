package milkman.plugin.note;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import milkman.domain.RequestAspect;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;

import static milkman.utils.FunctionalUtils.*;

public class NotesAspectEditor implements RequestAspectEditor {

	@Override
	public Tab getRoot(RequestAspect aspect) {
		NotesAspect notes = (NotesAspect) aspect;
		Tab tab = new Tab("Notes");
		TextArea textArea = new TextArea();
		GenericBinding<NotesAspect, String> binding = GenericBinding.of(
				NotesAspect::getNote, 
				run(NotesAspect::setNote)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				notes); 
		textArea.textProperty().bindBidirectional(binding);
		textArea.setUserData(binding); //need to add a strong reference to keep the binding from being GC-collected.
		tab.setContent(textArea);
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestAspect aspect) {
		return aspect instanceof NotesAspect;
	}

}
