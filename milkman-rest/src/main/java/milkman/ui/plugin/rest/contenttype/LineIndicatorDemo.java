package milkman.ui.plugin.rest.contenttype;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import milkman.ui.components.CodeFoldingContentEditor;
import milkman.ui.components.CodeFoldingContentEditor.CodeFoldingBuilder;
import milkman.ui.components.CodeFoldingContentEditor.CollapsableRange;
import milkman.ui.components.CodeFoldingContentEditor.ContentRange;
import milkman.ui.components.CodeFoldingContentEditor.TextRange;
import milkman.ui.plugin.ContentTypePlugin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Demonstrates the usage of
 * {@link StyledTextArea#paragraphGraphicFactoryProperty()}.
 */
public class LineIndicatorDemo extends Application {

	public static final String JSON_TEXT = loadFile();

	@SneakyThrows
	private static String loadFile() {
		return IOUtils.toString(LineIndicatorDemo.class.getResourceAsStream("/test.json"));
	}


	private CodeFoldingContentEditor codeArea;

	@Data
	@AllArgsConstructor
	public static class ContentBean {
		String text;
	}

	@Override
	public void start(Stage primaryStage) {
		codeArea = new CodeFoldingContentEditor();

		codeArea.setContentTypePlugins(Arrays.asList(new JsonContentType()));

		codeArea.setContentType("application/json");
		codeArea.setEditable(false);
		ContentBean bean = new ContentBean(JSON_TEXT);

		codeArea.setContent(bean::getText, bean::setText);

		var scene = new Scene(new StackPane(codeArea), 600, 400);
		scene.getStylesheets().add("/themes/milkman.css");
		scene.getStylesheets().add("/themes/syntax/milkman-syntax.css");
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}



}