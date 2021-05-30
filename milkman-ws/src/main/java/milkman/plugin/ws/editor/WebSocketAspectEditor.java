package milkman.plugin.ws.editor;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import milkman.ctrl.ExecutionListenerManager;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.ws.domain.WebsocketAspect;
import milkman.plugin.ws.domain.WebsocketResponseAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.contenttype.PlainContentTypePlugin;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.contenttype.JsonContentType;

import java.util.Arrays;

import static milkman.utils.FunctionalUtils.run;

public class WebSocketAspectEditor implements RequestAspectEditor, ExecutionListenerAware  {

	private ExecutionListenerManager executionListenerManager;

	@Override
	public Tab getRoot(RequestContainer request) {
		WebsocketAspect wsAspect = request.getAspect(WebsocketAspect.class).get();
		Tab tab = new Tab("Messages");
		
		ContentEditor editor = new ContentEditor();
		editor.setHeaderVisibility(false);
		editor.setEditable(true);
		editor.setContentTypePlugins(Arrays.asList(new JsonContentType(), new PlainContentTypePlugin()));
		editor.setContentType("text/plain");
		editor.setContent(wsAspect::getMessage, run(wsAspect::setMessage).andThen(() -> wsAspect.setDirty(true)));

		var addItemBtn = new JFXButton();
		addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE, "1.5em"));
		addItemBtn.getStyleClass().add("btn-add-entry");
		addItemBtn.setDisable(true);
		StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));

		executionListenerManager.listenOnExecution(request, "ws-msg-sender-listener", new ExecutionListenerAware.ExecutionListener() {
			@Override
			public void onRequestStarted(RequestContainer request, ResponseContainer response) {
				addItemBtn.setDisable(false);
				addItemBtn.setOnAction(e -> response.getAspect(WebsocketResponseAspect.class).ifPresent(ws -> {
					if (ws.getClient().isOpen()) {
						ws.getClient().send(wsAspect.getMessage());
					}
				}));
			}

			@Override
			public void onRequestFinished(RequestContainer request, ResponseContainer response) {
				addItemBtn.setDisable(true);
			}
		});


		tab.setContent(new StackPane(editor, addItemBtn));
		return tab;
	}

	@Override
	public void setExecutionListenerManager(ExecutionListenerManager manager) {
		executionListenerManager = manager;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(WebsocketAspect.class).isPresent();
	}

}
