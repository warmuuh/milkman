package milkman.ui.plugin.rest.curl;

import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.SystemUtils;

@RequiredArgsConstructor
public abstract class AbstractTextExporter implements RequestExporterPlugin {

	private TextArea textArea;

	private boolean isWindows = SystemUtils.IS_OS_WINDOWS;

	private RequestContainer request;

	private final String name;
	private final TextExport export;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof RestRequestContainer;
	}

	@Override
	public Node getRoot(RequestContainer request, Templater templater) {
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
				refreshCommand(templater);
			}
		});

		osSelection.getChildren().add(windowsTgl);
		osSelection.getChildren().add(unixTgl);
		
		VBox root = new VBox();
		root.getChildren().add(osSelection);
		root.getChildren().add(textArea);
		VBox.setVgrow(textArea, Priority.ALWAYS);
		refreshCommand(templater);
		return 	root;
	}

	public void refreshCommand(Templater templater) {
		textArea.setText(export.export(isWindows, (RestRequestContainer) request, templater));
	}

	@Override
	public boolean isAdhocExporter() {
		return true;
	}

	@Override
	public boolean doExport(RequestContainer request, Templater templater, Toaster toaster) {
		throw new UnsupportedOperationException("not implemented bc adhoc exporter");
	}

}
