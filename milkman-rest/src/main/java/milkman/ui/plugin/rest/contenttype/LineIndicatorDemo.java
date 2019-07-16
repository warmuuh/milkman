package milkman.ui.plugin.rest.contenttype;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.ui.components.CodeFoldingContentEditor;
import milkman.ui.components.CodeFoldingContentEditor.CodeFoldingBuilder;
import milkman.ui.components.CodeFoldingContentEditor.CollapsableRange;
import milkman.ui.components.CodeFoldingContentEditor.ContentRange;
import milkman.ui.components.CodeFoldingContentEditor.TextRange;
import milkman.ui.plugin.ContentTypePlugin;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Demonstrates the usage of
 * {@link StyledTextArea#paragraphGraphicFactoryProperty()}.
 */
public class LineIndicatorDemo extends Application {

	public static final String JSON_TEXT = "{\n" +
			"  \"id\" : 1,\n" +
			"  \"name\" : \"Leanne Graham\",\n" +
			"  \"username\" : \"Bret\",\n" +
			"  \"email\" : \"Sincere@april.biz\",\n" +
			"  \"address\" : {\n" +
			"    \"street\" : \"Kulas Light\",\n" +
			"    \"suite\" : \"Apt. 556\",\n" +
			"    \"city\" : \"Gwenborough\",\n" +
			"    \"zipcode\" : \"92998-3874\",\n" +
			"    \"geo\" : {\n" +
			"      \"lat\" : \"-37.3159\",\n" +
			"      \"lng\" : \"81.1496\"\n" +
			"    }\n" +
			"  },\n" +
			"  \"phone\" : \"1-770-736-8031 x56442\",\n" +
			"  \"website\" : \"hildegard.org\",\n" +
			"  \"company\" : {\n" +
			"    \"name\" : \"Romaguera-Crona\",\n" +
			"    \"catchPhrase\" : \"Multi-layered client-server neural-net\",\n" +
			"    \"bs\" : \"harness real-time e-markets\"\n" +
			"  }\n" +
			"}";




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
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}



}