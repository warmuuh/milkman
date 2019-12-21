package milkman.ui.plugin.rest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.CodeFoldingContentEditor;
import milkman.ui.components.ContentEditor;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkman.utils.reactive.Subscribers;
import reactor.core.scheduler.Schedulers;

public class ResponseBodyTabController implements ResponseAspectEditor, ContentTypeAwareEditor {

	private List<ContentTypePlugin> plugins;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val body = response.getAspect(RestResponseBodyAspect.class).get();
		CodeFoldingContentEditor root = new CodeFoldingContentEditor();
		root.setEditable(false);
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		
		response.getAspect(RestResponseHeaderAspect.class)
				.map(RestResponseHeaderAspect::contentType)
				.ifPresent(root::setContentType);
		

		
		AtomicInteger idx = new AtomicInteger(0);
		body.getBody()
				.subscribeOn(Schedulers.boundedElastic())
				.subscribe(
				value -> {
					val cidx = idx.getAndIncrement();
//					System.out.println("receiving content: " + cidx );
					Platform.runLater(() -> {
//						System.out.println("adding content" + cidx + " " + value);
						root.addContent(value);
					});
				},
				throwable -> {
					throwable.printStackTrace();
					Platform.runLater(() -> {

						root.addContent(throwable.toString());
					});
				},
				() -> Platform.runLater(() -> {
					if (CoreApplicationOptionsProvider.options().isAutoformatContent())
						root.formatCurrentCode();
				}),
				s -> {
//					System.out.println("Subscribed");
					s.request(Long.MAX_VALUE);
				}
			);
		
		return new Tab("Response Body", root);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(RestResponseBodyAspect.class).isPresent();
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		this.plugins = plugins;
		
	}

	
}
