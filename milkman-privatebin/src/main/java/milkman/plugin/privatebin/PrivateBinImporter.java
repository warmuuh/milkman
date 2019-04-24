package milkman.plugin.privatebin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXTextField;

import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;

@Slf4j
public class PrivateBinImporter implements ImporterPlugin {

	private JFXTextField urlTxt;
	private PrivateBinApi api = new PrivateBinApi(PrivateBinOptionsPluginProvider.options().getPrivateBinUrl());

	@Override
	public String getName() {
		return "Shared with PrivateBin";
	}

	@Override
	public Node getImportControls() {
		urlTxt = new JFXTextField();
		urlTxt.setPromptText("Paste your PrivateBin Url here");
		return urlTxt;
	}

	@Override
	public boolean importInto(Workspace workspace, Toaster toaster) {
		ObjectMapper objectMapper = createMapper();
		try {
			String json = api.readPaste(urlTxt.getText());
			RequestContainer request = objectMapper.readValue(json, RequestContainer.class);
			workspace.getOpenRequests().add(request);
			workspace.setActiveRequest(request);
			return true;
		} catch (Exception e) {
			toaster.showToast("Failed to read request");
			log.error("Failed to read request", e);
		}
		return false;
	}

	private ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		return mapper;
	}

}
