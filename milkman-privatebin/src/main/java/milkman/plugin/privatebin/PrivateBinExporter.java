package milkman.plugin.privatebin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.RequestExporterPlugin;
import milkman.ui.plugin.Templater;

@Slf4j
public class PrivateBinExporter implements RequestExporterPlugin {

	private JFXTextField textField;
	private PrivateBinApi api = new PrivateBinApi(PrivateBinOptionsPluginProvider.options().getPrivateBinUrl());
	private JFXCheckBox burnCheckbox;
	
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
		textField = new JFXTextField();
		textField.setEditable(false);
		burnCheckbox = new JFXCheckBox("Burn after reading");
		VBox vBox = new VBox(burnCheckbox, textField);
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
