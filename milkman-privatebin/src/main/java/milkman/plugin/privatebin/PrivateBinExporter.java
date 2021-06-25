package milkman.plugin.privatebin;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.Templater;
import milkman.utils.fxml.facade.FxmlBuilder;

@Slf4j
public class PrivateBinExporter implements RequestExporterPlugin {

	private TextField textField;
	private final PrivateBinApi api = new PrivateBinApi(PrivateBinOptionsPluginProvider.options().getPrivateBinUrl());
	private CheckBox burnCheckbox;
	private CheckBox resolveCheckbox;

	
	@Override
	public String getName() {
		return "Share via PrivateBin";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return true;
	}

	@Override
	public Node getRoot(RequestContainer request, Templater templater) {
		textField = FxmlBuilder.text();
		textField.setEditable(false);
		burnCheckbox = FxmlBuilder.checkbox("Burn after reading");
		resolveCheckbox = FxmlBuilder.checkbox("resolve variables");
		resolveCheckbox.setSelected(true);
		VBox vBox = new VBox(new HBox(burnCheckbox, resolveCheckbox), textField);
		return vBox;
	}

	@Override
	public boolean isAdhocExporter() {
		return false;
	}

	@Override
	public boolean doExport(RequestContainer request, Templater templater, Toaster toaster) {
		ObjectMapper objectMapper = createMapper();
		try {
			String content = objectMapper.writeValueAsString(new RequestDataContainer(request));
			if (resolveCheckbox.isSelected()){
				content = templater.replaceTags(content);
			}
			String pasteUrl = api.createPaste(content, burnCheckbox.isSelected());
			textField.setText(pasteUrl);
		} catch (Exception e) {
			toaster.showToast("Failed to store request");
			log.error("Failed to store request", e);
		}
		return false;
	}

	private ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		return mapper;
	}
	
}
