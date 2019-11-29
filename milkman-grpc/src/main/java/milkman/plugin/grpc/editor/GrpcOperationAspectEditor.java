package milkman.plugin.grpc.editor;

import static milkman.utils.FunctionalUtils.run;

import java.util.Collections;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.GrpcContentType;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.ui.components.AutoCompleter;
import milkman.ui.components.ContentEditor;
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
		
		TextField textField = new JFXTextField();
		GenericBinding<GrpcOperationAspect, String> binding = GenericBinding.of(
				GrpcOperationAspect::getOperation, 
				run(GrpcOperationAspect::setOperation)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				aspect); 
		textField.setPromptText("<package>.<service>/<method>");
		textField.textProperty().bindBidirectional(binding);
		textField.setUserData(binding); //need to add a strong reference to keep the binding from being GC-collected.
		HBox.setHgrow(textField, Priority.ALWAYS);
		
		completer.attachVariableCompletionTo(textField);
		
	
		ContentEditor contentView = new ContentEditor();
		contentView.setEditable(true);
		contentView.setContentTypePlugins(Collections.singletonList(new GrpcContentType()));
		contentView.setContentType("application/vnd.wrm.protoschema");
		contentView.setHeaderVisibility(false);
		contentView.setContent(aspect::getProtoSchema, run(aspect::setProtoSchema).andThen(v -> aspect.setDirty(true)));
		VBox.setVgrow(contentView, Priority.ALWAYS);


		CheckBox useReflection = new JFXCheckBox("Use Reflection");
		GenericBinding<GrpcOperationAspect, Boolean> reflBinding = GenericBinding.of(
				GrpcOperationAspect::isUseReflection, 
				run(GrpcOperationAspect::setUseReflection)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				aspect); 

		useReflection.selectedProperty().addListener((observable, oldValue, newValue) -> {
			contentView.setDisableContent(newValue);
		});
		
		useReflection.selectedProperty().bindBidirectional(reflBinding);
		useReflection.setUserData(reflBinding); //need to add a strong reference to keep the binding from being GC-collected.

		
		
		return new Tab("Operation", new VBox(new HBox(10, new Label("Operation:"), textField, useReflection), new Label("Protobuf Schema:"), contentView));
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GrpcOperationAspect.class).isPresent();
	}





}
