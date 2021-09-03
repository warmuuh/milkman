package milkman.plugin.privatebin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.persistence.UnknownPluginHandler;
import milkman.ui.main.Toaster;
import milkman.ui.main.dialogs.ConfirmationInputDialog;
import milkman.ui.plugin.ImporterPlugin;

@Slf4j
public class PrivateBinImporter implements ImporterPlugin {

	private JFXTextField urlTxt;
	private final PrivateBinApi api = new PrivateBinApi(PrivateBinOptionsPluginProvider.options().getPrivateBinUrl());

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
			AbstractDataContainer container = objectMapper.readValue(json, AbstractDataContainer.class);
			if (container instanceof RequestDataContainer) {
				handleImportedRequest(workspace, ((RequestDataContainer) container).getRequest(), toaster);
			} else if (container instanceof CollectionDataContainer) {
				handleImportedCollection(workspace, ((CollectionDataContainer) container).getCollection(), toaster);
			} else if (container instanceof WorkspaceDataContainer) {
				handleImportedWorkspace(workspace, ((WorkspaceDataContainer) container).getWorkspace(), toaster);
			} else {
				toaster.showToast("Unknown data format");
			}
			return true;
		} catch (Exception e) {
			toaster.showToast("Failed to read request");
			log.error("Failed to read request", e);
		}
		return false;
	}

	private void handleImportedCollection(Workspace workspace, Collection collection, Toaster toast) {
		workspace.getCollections().add(collection);
		toast.showToast("Collection imported: " + collection.getName());
	}


	private void handleImportedWorkspace(Workspace activeWorkspace, Workspace newWorkspace, Toaster toast) {
		var dialog = new ConfirmationInputDialog();
		dialog.showAndWait("Confirm import", "The workspace '" + newWorkspace.getName() + "' will be imported into your current workspace.");
		if (!dialog.isCancelled()){
			activeWorkspace.getCollections().addAll(newWorkspace.getCollections());
			activeWorkspace.getEnvironments().addAll(newWorkspace.getEnvironments());
			toast.showToast("Workspace imported: " + newWorkspace.getName());
		}
	}

	private void handleImportedRequest(Workspace workspace, RequestContainer request, Toaster toast) {
		workspace.getOpenRequests().add(request);
		workspace.setActiveRequest(request);
		toast.showToast("Request imported: " + request.getName());
	}

	private ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		return mapper;
	}

}
