package milkman.plugin.sio.editor;

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
import milkman.plugin.sio.domain.SocketIOAspect;
import milkman.plugin.sio.domain.SocketIOResponseAspect;
import milkman.ui.components.ContentEditor;
import milkman.ui.contenttype.PlainContentTypePlugin;
import milkman.ui.plugin.ExecutionListenerAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.contenttype.JsonContentType;

import java.util.Arrays;
import java.util.Optional;

import static milkman.utils.FunctionalUtils.run;

public class SocketIOAspectEditor implements RequestAspectEditor, ExecutionListenerAware {

    private ExecutionListenerManager executionListenerManager;


    @Override
    public Tab getRoot(RequestContainer request) {
        return getRoot(request, Optional.empty());
    }

    @Override
    public Tab getRoot(RequestContainer request, Optional<ResponseContainer> existingResponse) {
        SocketIOAspect sioAspect = request.getAspect(SocketIOAspect.class).get();
        Tab tab = new Tab("Messages");

        ContentEditor editor = new ContentEditor();
        editor.setHeaderVisibility(false);
        editor.setEditable(true);
        editor.setContentTypePlugins(Arrays.asList(new JsonContentType(), new PlainContentTypePlugin()));
        editor.setContentType("text/plain");
        editor.setContent(sioAspect::getMessage, run(sioAspect::setMessage).andThen(() -> sioAspect.setDirty(true)));

        var addItemBtn = new JFXButton();
        addItemBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        addItemBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PAPER_PLANE, "1.5em"));
        addItemBtn.getStyleClass().add("btn-add-entry");

        addItemBtn.setDisable(true);

        activateIfActive(sioAspect, addItemBtn, existingResponse);


        StackPane.setAlignment(addItemBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(addItemBtn, new Insets(0, 20, 20, 0));

        executionListenerManager.listenOnExecution(request, "sio-msg-sender-listener", new ExecutionListener() {
            @Override
            public void onRequestStarted(RequestContainer request, ResponseContainer response) {
            }

            @Override
            public void onRequestReady(RequestContainer request, ResponseContainer response) {
                addItemBtn.setDisable(false);
                addItemBtn.setOnAction(e -> response.getAspect(SocketIOResponseAspect.class).ifPresent(sio -> {
                    if (sio.getClient().connected()) {
                        sio.getClient().emit(sioAspect.getMessage());
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

    private void activateIfActive(SocketIOAspect sioAspect, JFXButton addItemBtn, Optional<ResponseContainer> existingResponse) {
        existingResponse.ifPresent(response -> response.getAspect(SocketIOResponseAspect.class)
                .filter(sio -> sio.getClient().connected())
                .ifPresent(sio -> {
                    addItemBtn.setDisable(false);
                    addItemBtn.setOnAction(e -> {
                        if (sio.getClient().connected()) {
                            sio.getClient().emit(sioAspect.getMessage());
                        }
                    });
                }));
    }


    @Override
    public void setExecutionListenerManager(ExecutionListenerManager manager) {
        executionListenerManager = manager;
    }

    @Override
    public boolean canHandleAspect(RequestContainer request) {
        return request.getAspect(SocketIOAspect.class).isPresent();
    }

}
