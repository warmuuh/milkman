package milkman.plugin.grpc.editor;

import java.nio.charset.StandardCharsets;
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
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

public class GrpcResponsePayloadEditor implements ResponseAspectEditor, ContentTypeAwareEditor {


	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val payload = response.getAspect(GrpcResponsePayloadAspect.class).orElseThrow(() -> new IllegalArgumentException("No Grpc payload aspect"));

		ContentEditor root = new CodeFoldingContentEditor();
		root.setEditable(false);
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		root.setContentType("application/json");

		payload.getPayloads().subscribe(
			value -> {
				Platform.runLater(() -> {
					root.addContent("\n");
					root.addContent(new String(value, StandardCharsets.UTF_8));
				});
			},
			throwable -> {
				Platform.runLater(() -> {
					root.addContent("\n");
					root.addContent(ExceptionUtils.getRootCauseMessage(throwable));
				});
			}
		);
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
