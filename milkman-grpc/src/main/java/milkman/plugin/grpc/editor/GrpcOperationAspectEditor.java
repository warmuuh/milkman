package milkman.plugin.grpc.editor;

import static milkman.utils.FunctionalUtils.run;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
		
	
		
		TextArea textArea = new TextArea();
		GenericBinding<GrpcOperationAspect, String> schemaBinding = GenericBinding.of(
				GrpcOperationAspect::getProtoSchema, 
				run(GrpcOperationAspect::setProtoSchema)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				aspect); 

		textArea.textProperty().bindBidirectional(schemaBinding);
		textArea.setUserData(schemaBinding); //need to add a strong reference to keep the binding from being GC-collected.
		VBox.setVgrow(textArea, Priority.ALWAYS);
		
		CheckBox useReflection = new CheckBox("Use Reflection");
		GenericBinding<GrpcOperationAspect, Boolean> reflBinding = GenericBinding.of(
				GrpcOperationAspect::isUseReflection, 
				run(GrpcOperationAspect::setUseReflection)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				aspect); 

		useReflection.selectedProperty().addListener((observable, oldValue, newValue) -> {
			textArea.setDisable(newValue);
		});
		
		useReflection.selectedProperty().bindBidirectional(reflBinding);
		useReflection.setUserData(reflBinding); //need to add a strong reference to keep the binding from being GC-collected.

		
		
		return new Tab("Operation", new VBox(textField, useReflection, textArea));
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GrpcOperationAspect.class).isPresent();
	}


}
