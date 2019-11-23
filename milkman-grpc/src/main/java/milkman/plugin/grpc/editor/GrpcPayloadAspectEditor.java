package milkman.plugin.grpc.editor;

import static milkman.utils.FunctionalUtils.run;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcPayloadAspect;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;

public class GrpcPayloadAspectEditor implements RequestAspectEditor {



	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		GrpcPayloadAspect aspect = request.getAspect(GrpcPayloadAspect.class).get();
		TextArea textArea = new TextArea();
		GenericBinding<GrpcPayloadAspect, String> binding = GenericBinding.of(
				GrpcPayloadAspect::getPayload, 
				run(GrpcPayloadAspect::setPayload)
					.andThen(() -> aspect.setDirty(true)), //mark aspect as dirty propagates to the request itself and shows up in UI
				aspect); 

		textArea.textProperty().bindBidirectional(binding);
		textArea.setUserData(binding); //need to add a strong reference to keep the binding from being GC-collected.
		
		return new Tab("Payload", textArea);
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GrpcPayloadAspect.class).isPresent();
	}


}
