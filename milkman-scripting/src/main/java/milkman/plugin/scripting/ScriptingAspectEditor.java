package milkman.plugin.scripting;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import lombok.SneakyThrows;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.plugin.scripting.conenttype.JavascriptContentType;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.RequestAspectEditor;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static milkman.utils.FunctionalUtils.run;

public class ScriptingAspectEditor implements RequestAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		val script = request.getAspect(ScriptingAspect.class).get();

		Tab preTab = getTab(script::getPreRequestScript,
				run(script::setPreRequestScript).andThen(() -> script.setDirty(true)),
				"Before Request");
		Tab postTab = getTab(script::getPostRequestScript,
				run(script::setPostRequestScript).andThen(() -> script.setDirty(true)),
				"After Request");
		TabPane tabs = new TabPane(preTab, postTab);
		tabs.getSelectionModel().select(1); //default to show after request script

		tabs.setSide(Side.LEFT);
		tabs.getStyleClass().add("options-tabs");
		tabs.tabMinWidthProperty().setValue(20);
		tabs.tabMaxWidthProperty().setValue(20);
		tabs.tabMinHeightProperty().setValue(150);
		tabs.tabMaxHeightProperty().setValue(150);
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);


		return new Tab("Scripting", tabs);
	}

	private Tab getTab(Supplier<String> getter, Consumer<String> setter, String title) {
		ContentEditor postEditor = new ContentEditor();
		postEditor.setEditable(true);
		postEditor.setContent(getter, setter);
		postEditor.setContentTypePlugins(Collections.singletonList(new JavascriptContentType()));
		postEditor.setContentType("application/javascript");
		postEditor.setHeaderVisibility(false);

		Tab postTab = new Tab("", postEditor);
		Label label = new Label(title);
		label.setRotate(90);
		label.setMinWidth(150);
		label.setMaxWidth(150);
		label.setMinHeight(40);
		label.setMaxHeight(40);
		label.setPadding(new Insets(0));
		postTab.setGraphic(label);
		return postTab;
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(ScriptingAspect.class).isPresent();
	}

}
