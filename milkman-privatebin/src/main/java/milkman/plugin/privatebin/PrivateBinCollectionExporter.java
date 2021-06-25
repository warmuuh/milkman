package milkman.plugin.privatebin;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.CollectionExporterPlugin;
import milkman.ui.plugin.Templater;
import milkman.utils.fxml.facade.FxmlBuilder;

@Slf4j
public class PrivateBinCollectionExporter implements CollectionExporterPlugin {

	private TextField textField;
	private final PrivateBinApi api = new PrivateBinApi(PrivateBinOptionsPluginProvider.options().getPrivateBinUrl());
	private CheckBox burnCheckbox;
	
	@Override
	public String getName() {
		return "Share via PrivateBin";
	}


	@Override
	public Node getRoot(Collection collection, Templater templater) {
		textField = FxmlBuilder.text();
		textField.setEditable(false);
		burnCheckbox = FxmlBuilder.checkbox("Burn after reading");
		VBox vBox = new VBox(burnCheckbox, textField);
		return vBox;
	}

	@Override
	public boolean isAdhocExporter() {
		return false;
	}

	@Override
	public boolean doExport(Collection collection, Templater templater, Toaster toaster) {
		ObjectMapper objectMapper = createMapper();
		try {
			String content = objectMapper.writeValueAsString(new CollectionDataContainer(collection));
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
