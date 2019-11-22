package milkman.plugin.grpc.editor;

import java.util.List;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;

public class GrpcResponsePayloadEditor implements ResponseAspectEditor {


	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val payload = response.getAspect(GrpcResponsePayloadAspect.class).get();
		TextArea root = new TextArea();
		root.setEditable(false);
		payload.getPayloads().subscribe(
			value -> root.setText(root.getText() + "\n" + value),
			throwable -> root.setText(root.getText() + "\n" + throwable.toString())
		);
		VBox.setVgrow(root, Priority.ALWAYS);
		return new Tab("Response Payload", root);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(GrpcResponsePayloadAspect.class).isPresent();
	}

}
