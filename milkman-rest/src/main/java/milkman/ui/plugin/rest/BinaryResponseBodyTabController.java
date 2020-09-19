package milkman.ui.plugin.rest;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.ToasterAware;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkman.utils.BinaryUtil;
import milkman.utils.fxml.FxmlUtil;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static milkman.utils.fxml.FxmlBuilder.*;

@Slf4j
public class BinaryResponseBodyTabController implements ResponseAspectEditor, ToasterAware {

	private final static List<String> supportedContentTypes = List.of(
			"image/png",
			"image/bmp",
			"image/gif",
			"image/jpeg"
	);

	private Toaster toaster;

	private byte[] fileContent = null;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		val body = response.getAspect(RestResponseBodyAspect.class).orElseThrow(() -> new IllegalArgumentException("No rest response aspect"));
		ImageView imageView = new ImageView();


		body.getBody()
				.subscribeOn(Schedulers.elastic())
				.collectList()
				.map(BinaryUtil::concat)
				.subscribe(bytes -> {
							fileContent = bytes;
							Platform.runLater(() -> {
								var image = new Image(new ByteArrayInputStream(bytes));
								if (!image.isError()){
									imageView.setImage(image);
								}
							});
						},
						throwable -> {
							log.error("Received Error", throwable);
							toaster.showToast(throwable.toString());
						},
						() -> {
						},
						s -> {
							fileContent = null;
							s.request(Long.MAX_VALUE);
						}
				);


		var header = hbox("preview-header");
		header.add(button("preview-download", icon(FontAwesomeIcon.DOWNLOAD, "1.5em"), () ->{
			var filenameProposal = getFilenameProposal((RestRequestContainer) request);
			saveFile(filenameProposal);
		}));


		var content = vbox("preview-area");
		content.add(header);
		content.add(new ScrollPane(imageView), true);

		return new Tab("Response Preview", content);
	}


	private String getFilenameProposal(RestRequestContainer request) {
		try {
			String path = new URI(request.getUrl()).getPath();
			var idx = path.lastIndexOf("/");
			if (idx < 0) {
				return "";
			}
			return path.substring(idx+1);
		} catch (URISyntaxException e) {
			return "";
		}
	}

	private void saveFile(String filenameProposal) {
		if (fileContent == null){
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(filenameProposal);
		fileChooser.setTitle("Save File...");
		File file = fileChooser.showSaveDialog(FxmlUtil.getPrimaryStage());
		if (file != null) {
			try (var fs = new FileOutputStream(file)) {
				fs.write(fileContent);
			} catch (IOException ex) {
				toaster.showToast(ex.toString());
			}
		}
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(RestResponseBodyAspect.class).isPresent()
				&& response.getAspect(RestResponseHeaderAspect.class)
				.map(a -> isBinary(a.contentType()))
				.orElse(false);
	}

	public static boolean isBinary(String contentType) {
		return supportedContentTypes.stream().anyMatch(sct -> contentType.toLowerCase().contains(sct));
	}

	@Override
	public void setToaster(Toaster toaster) {
		this.toaster = toaster;
	}
}
