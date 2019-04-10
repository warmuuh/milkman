package milkman.plugin.note;

import static milkman.utils.FunctionalUtils.run;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;

public class NotesAspectEditor implements RequestAspectEditor {

	@Override
	public Tab getRoot(RequestContainer request) {
		NotesAspect notes = request.getAspect(NotesAspect.class).get();
		Tab tab = new Tab("Notes");
		TextArea textArea = new TextArea();
		GenericBinding<NotesAspect, String> binding = GenericBinding.of(
				NotesAspect::getNote, 
				run(NotesAspect::setNote)
					.andThen(() -> notes.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				notes); 
		textArea.textProperty().bindBidirectional(binding);
		textArea.setUserData(binding); //need to add a strong reference to keep the binding from being GC-collected.
		tab.setContent(textArea);
		return tab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(NotesAspect.class).isPresent();
	}

}
