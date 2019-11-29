package milkman.plugin.grpc.editor;

import static milkman.utils.FunctionalUtils.run;

import java.util.List;

import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcPayloadAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.RequestAspectEditor;

public class GrpcPayloadAspectEditor implements RequestAspectEditor, ContentTypeAwareEditor{
	private List<ContentTypePlugin> contentTypes;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		GrpcPayloadAspect aspect = request.getAspect(GrpcPayloadAspect.class).get();
		
		ContentEditor root = new ContentEditor();
		root.setEditable(true);
		root.setContent(aspect::getPayload, run(aspect::setPayload).andThen(() -> aspect.setDirty(true)));
		if (contentTypes != null)
			root.setContentTypePlugins(contentTypes);
		root.setContentType("application/json");
		root.setHeaderVisibility(false);
		return new Tab("Payload", root);
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GrpcPayloadAspect.class).isPresent();
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> contentTypes) {
		this.contentTypes = contentTypes;
	}
}
