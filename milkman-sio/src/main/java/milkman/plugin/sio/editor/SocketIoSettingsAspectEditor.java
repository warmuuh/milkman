package milkman.plugin.sio.editor;

import static milkman.utils.fxml.facade.FxmlBuilder.VboxExt;
import static milkman.utils.fxml.facade.FxmlBuilder.comboBox;
import static milkman.utils.fxml.facade.FxmlBuilder.hbox;
import static milkman.utils.fxml.facade.FxmlBuilder.label;
import static milkman.utils.fxml.facade.FxmlBuilder.text;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.plugin.sio.domain.SocketIoSettingsAspect;
import milkman.plugin.sio.domain.SocketIoVersion;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.utils.fxml.GenericBinding;
import milkman.utils.fxml.facade.SimpleComboBox;

@Slf4j
public class SocketIoSettingsAspectEditor implements RequestAspectEditor {

	private TextField txtHandshakePath;
	private SimpleComboBox<SocketIoVersion> cbSocketIoVersion;

	private final GenericBinding<SocketIoSettingsAspect, String> handshakePathBinding =
			GenericBinding.of(SocketIoSettingsAspect::getHandshakePath, SocketIoSettingsAspect::setHandshakePath);
	private final GenericBinding<SocketIoSettingsAspect, SocketIoVersion> protocolBinding =
			GenericBinding.of(SocketIoSettingsAspect::getClientVersion, SocketIoSettingsAspect::setClientVersion);


	@Override
	public Tab getRoot(RequestContainer request) {
		SocketIoSettingsAspect settings = request.getAspect(SocketIoSettingsAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("missing test aspect"));

		var content = new SocketIoSettingsAspectEditorFxml(this);

		handshakePathBinding.bindTo(txtHandshakePath.textProperty(), settings);
		handshakePathBinding.addListener(c -> request.setDirty(true));

		protocolBinding.bindTo(cbSocketIoVersion.valueProperty(), settings);
		protocolBinding.addListener(c -> request.setDirty(true));

		return new Tab("Settings", content);
	}


	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(SocketIoSettingsAspect.class).isPresent();
	}


	public static class SocketIoSettingsAspectEditorFxml extends VboxExt {
		private final SocketIoSettingsAspectEditor controller;

		public SocketIoSettingsAspectEditorFxml(SocketIoSettingsAspectEditor controller) {
			this.controller = controller;
			add(new Label("Socket.IO Settings"));
			controller.txtHandshakePath = text("handshakePath", "/socket.io");
			add(hbox(label("Handshake path"), controller.txtHandshakePath));

			controller.cbSocketIoVersion = comboBox("socketIoVersion");
			controller.cbSocketIoVersion.getItems().addAll(SocketIoVersion.values());
			add(hbox(label("Socket.io Version"), controller.cbSocketIoVersion));

			getStyleClass().add("generic-content-pane");

		}
	}



}
