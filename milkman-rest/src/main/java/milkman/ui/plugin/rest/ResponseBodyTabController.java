package milkman.ui.plugin.rest;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.CodeFoldingContentEditor;
import milkman.ui.main.Toaster;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.ContentTypeAwareEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.ToasterAware;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ResponseBodyTabController implements ResponseAspectEditor, ContentTypeAwareEditor, ToasterAware {

	private List<ContentTypePlugin> plugins;
	private Toaster toaster;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val body = response.getAspect(RestResponseBodyAspect.class).orElseThrow(() -> new IllegalArgumentException("No rest response aspect"));
		CodeFoldingContentEditor root = new CodeFoldingContentEditor();
		root.setEditable(false);
		if (plugins != null)
			root.setContentTypePlugins(plugins);
		
		response.getAspect(RestResponseHeaderAspect.class)
				.map(RestResponseHeaderAspect::contentType)
				.ifPresent(root::setContentType);
		

		
		AtomicInteger idx = new AtomicInteger(0);
		body.getBody()
				.subscribeOn(Schedulers.elastic())
				.subscribe(
				value -> {
					val cidx = idx.getAndIncrement();
//					System.out.println("receiving content: " + cidx );
					Platform.runLater(() -> {
//						System.out.println("adding content" + cidx + " " + value);
						root.addContent(new String(value, StandardCharsets.UTF_8));
					});
				},
				throwable -> {
					log.error("Received Error", throwable);
					toaster.showToast(throwable.toString());
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
		return response.getAspect(RestResponseBodyAspect.class).isPresent()
				&& response.getAspect(RestResponseHeaderAspect.class)
				.map(a -> !BinaryResponseBodyTabController.isBinary(a.contentType()))
				.orElse(true);
	}

	@Override
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		this.plugins = plugins;
		
	}


	@Override
	public void setToaster(Toaster toaster) {
		this.toaster = toaster;
	}
}
