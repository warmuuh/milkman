package milkman.plugin.privatebin;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Workspace;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.main.Toaster;
import milkman.ui.main.sync.NoSyncDetails;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.WorkspaceExporterPlugin;
import milkman.utils.ObjectUtils;
import milkman.utils.fxml.facade.FxmlBuilder;

@Slf4j
public class PrivateBinWorkspaceExporter implements WorkspaceExporterPlugin {

	private TextField textField;
	private final PrivateBinApi api = new PrivateBinApi(PrivateBinOptionsPluginProvider.options().getPrivateBinUrl());
	private CheckBox burnCheckbox;
	private CheckBox exportKeysCheckbox;
	
	@Override
	public String getName() {
		return "Share via PrivateBin";
	}


	@Override
	public Node getRoot(Workspace workspace, Templater templater) {
		textField = FxmlBuilder.text();
		textField.setEditable(false);
		burnCheckbox = FxmlBuilder.checkbox("Burn after reading");
		exportKeysCheckbox = FxmlBuilder.checkbox("Export keys");
		VBox vBox = new VBox(new HBox(exportKeysCheckbox, burnCheckbox), textField);
		return vBox;
	}

	@Override
	public boolean isAdhocExporter() {
		return false;
	}

	@Override
	public boolean doExport(Workspace workspace, Templater templater, Toaster toaster) {
		ObjectMapper objectMapper = createMapper();
		try {
			//we strip sync details as they might contain passwords
			var workspaceClone = ObjectUtils.deepClone(workspace);
			workspaceClone.setSyncDetails(new NoSyncDetails());
			if (!exportKeysCheckbox.isSelected()) {
				workspaceClone.setKeySets(Collections.emptyList());
			}
			String content = objectMapper.writeValueAsString(new WorkspaceDataContainer(workspaceClone));
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
