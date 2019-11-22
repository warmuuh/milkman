package milkman.plugin.grpc.editor;

import static milkman.utils.FunctionalUtils.run;

import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.ui.components.AutoCompleter;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;

public class GrpcOperationAspectEditor implements RequestAspectEditor, AutoCompletionAware {

	private AutoCompleter completer;


	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}


	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		GrpcOperationAspect aspect = request.getAspect(GrpcOperationAspect.class).get();
		TextField textField = new TextField();
		GenericBinding<GrpcOperationAspect, String> binding = GenericBinding.of(
				GrpcOperationAspect::getOperation, 
				run(GrpcOperationAspect::setOperation)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				aspect); 

		textField.textProperty().bindBidirectional(binding);
		textField.setUserData(binding); //need to add a strong reference to keep the binding from being GC-collected.
		
		completer.attachVariableCompletionTo(textField);
		
		HBox.setHgrow(textField, Priority.ALWAYS);
		
		
		return new Tab("Operation", textField);
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GrpcOperationAspect.class).isPresent();
	}


}
