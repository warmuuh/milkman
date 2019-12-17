package milkman.plugin.grpc.editor;

import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.ui.components.CodeFoldingContentEditor;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.utils.reactive.Subscribers;

public class GrpcResponsePayloadEditor implements ResponseAspectEditor, ContentTypeAwareEditor {


	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val payload = response.getAspect(GrpcResponsePayloadAspect.class).get();
//		TextArea root = new TextArea();
//		root.setEditable(false);
//		payload.getPayloads().subscribe(Subscribers.subscriber(
//			value -> root.setText(root.getText() + "\n" + value),
//			throwable -> root.setText(root.getText() + "\n" + throwable.toString())
//		));
//		VBox.setVgrow(root, Priority.ALWAYS);
		
		ContentEditor root = new CodeFoldingContentEditor();
		root.setEditable(false);
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		root.setContentType("application/json");
		
		payload.getPayloads().subscribe(Subscribers.subscriber(
			value -> {
				Platform.runLater(() -> {
					root.addContent("\n");
					root.addContent(value);
				});
			},
			throwable -> {
				Platform.runLater(() -> {
					root.addContent("\n");
					root.addContent(throwable.toString());
				});
			}
		));
		return new Tab("Response Payload", root);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(GrpcResponsePayloadAspect.class).isPresent();
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		this.plugins = plugins;
	}

}
