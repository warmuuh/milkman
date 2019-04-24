package milkman.ui.plugin.rest.export;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import io.vavr.NotImplementedError;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.RequestContainer;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class CurlExporter implements RequestExporterPlugin {

	private TextArea textArea;

	private boolean isWindows = SystemUtils.IS_OS_WINDOWS;

	private RequestContainer request;
	@Override
	public String getName() {
		return "Curl";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof RestRequestContainer;
	}

	@Override
	public Node getRoot(RequestContainer request) {
		this.request = request;
		textArea = new TextArea();
		textArea.setEditable(false);
		
		HBox osSelection = new HBox();
		
		ToggleGroup group = new ToggleGroup();
		RadioButton windowsTgl = new RadioButton("Windows");
		windowsTgl.setToggleGroup(group);
		windowsTgl.setSelected(isWindows);
		RadioButton unixTgl = new RadioButton("Unix");
		unixTgl.setSelected(!isWindows);
		unixTgl.setToggleGroup(group);
		
		
		windowsTgl.selectedProperty().addListener((obs, o, n) -> {
			if (n != null) {
				isWindows = n;
				refreshCommand();
			}
		});

		osSelection.getChildren().add(windowsTgl);
		osSelection.getChildren().add(unixTgl);
		
		VBox root = new VBox();
		root.getChildren().add(osSelection);
		root.getChildren().add(textArea);
		VBox.setVgrow(textArea, Priority.ALWAYS);
		refreshCommand();
		return 	root;
	}

	public void refreshCommand() {
		String curlCmd = toCurl((RestRequestContainer) request);
		textArea.setText(curlCmd);
	}
	private String toCurl(RestRequestContainer request) {
		
		String lineBreak = isWindows ? " ^\n  " : " \\\n  ";
		String quote = isWindows ? "\"" : "'";
		
		StringBuilder b = new StringBuilder();
		b.append("curl -X ");
		b.append(request.getHttpMethod());
		b.append(lineBreak);
		b.append(quote+request.getUrl()+quote);
		
		
		request.getAspect(RestHeaderAspect.class).ifPresent(a -> {
			if (!a.getEntries().isEmpty())
				b.append(lineBreak);		
			String headers = a.getEntries().stream()
			.filter(HeaderEntry::isEnabled)
			.map(h -> "-H " + quote + h.getName() + ": " + h.getValue() + quote)
			.collect(Collectors.joining(lineBreak));
			b.append(headers);
		});
		
		request.getAspect(RestBodyAspect.class).ifPresent(a -> {
			if (StringUtils.isNotBlank(a.getBody())) {
				b.append(lineBreak);		
				b.append("-d " + quote);b.append(a.getBody());b.append(quote);
			}
		});
		
		
		
		return b.toString();
	}

	@Override
	public boolean isAdhocExporter() {
		return true;
	}

	@Override
	public boolean doExport(RequestContainer request, Toaster toaster) {
		throw new NotImplementedError("not implemented bc adhoc exporter");
	}

}
