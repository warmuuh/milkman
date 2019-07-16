package milkman.ui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import milkman.ui.components.CodeFoldingContentEditor.CodeFoldingBuilder;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.ui.components.CodeFoldingContentEditor.CollapsableRange;
import milkman.ui.components.CodeFoldingContentEditor.ContentRange;
import milkman.ui.components.CodeFoldingContentEditor.TextRange;
import milkman.ui.plugin.ContentTypePlugin;

/**
 * Demonstrates the usage of
 * {@link StyledTextArea#paragraphGraphicFactoryProperty()}.
 */
public class LineIndicatorDemo extends Application {

	public static final String SIMPLE_TEXT = "This is a paragraph of text.\n"
			+ "(This is a nested collapsable paragraph of text.\n" + "This is a paragraph of text.)\n"
			+ "This is a paragraph of text.\n" + "\n" + "This is a paragraph of text.\n"
			+ "This is a paragraph of text.\n" + "This is a paragraph of text.\n" + "This is a paragraph of text.\n"
			+ "\n" + "This is a paragraph of text.\n" + "This is a paragraph of text.\n"
			+ "This is a paragraph of text.\n" + "This is a paragraph of text.\n" + "\n" + "Try it.";



	public static final String JSON_TEXT = "{\n" +
			"  \"paymentInstrument\" : {\n" +
			"    \"type\" : \"PayPalAccount\",\n" +
			"    \"description\" : \"r...n@outlook.com\"\n" +
			"  },\n" +
			"  \"changePaymentUrl\" : \"https://fundinginstrument.ebay.com.au/piapp/ebayplus/upfwallet\",\n" +
			"  \"membershipDetails\" : {\n" +
			"    \"nextPaymentDueDate\" : \"2019-11-14T07:11:03.000Z\",\n" +
			"    \"potentialCancellationDate\" : \"2019-11-14T07:11:03.000Z\",\n" +
			"    \"daysRemainingInTrial\" : 121,\n" +
			"    \"memberSince\" : \"2019-07-16T07:11:03.000Z\",\n" +
			"    \"cancelled\" : false\n" +
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

		codeArea.setContentTypePlugins(Arrays.asList(new JsonFoldingTypePlugin()));

		codeArea.setContentType("application/testjson");
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



	private static class JsonFoldingTypePlugin implements ContentTypePlugin {

		@Override
		public boolean supportFormatting() {
			return false;
		}

		@Override
		public String getName() {
			return "testjson";
		}

		@Override
		public String getContentType() {
			return "application/testjson";
		}

		@Override
		public String formatContent(String text) {
			return text;
		}

		@Override
		public StyleSpans<Collection<String>> computeHighlighting(String text) {
			return new StyleSpansBuilder().add(Collections.emptyList(), text.length()).create();
		}

		@Override
		public boolean supportFolding() {
			return true;
		}

		@Override
		public ContentRange computeFolding(String text) {
			return parseText(text);
		}

		protected CollapsableRange parseText(String text) {
			CodeFoldingBuilder folding = new CodeFoldingBuilder(text);

			char[] chars = text.toCharArray();
			int indentation = 0;
			
			for(int idx = 0; idx < chars.length; ++idx){
				char c = chars[idx];
				if(c == '{'){
					folding.startRange(idx, indent("{\n  …\n}", indentation));
					indentation += 2;
				} else if (c == '}') {
					indentation -= 2;
					idx += 1;
					folding.endRange(idx);
				}
			}

			return folding.build();
		}

		private String indent(String s, int indentation) {
			if (indentation == 0)
				return s;
			return s.replaceAll("(?m)^", StringUtils.repeat(' ', indentation)).trim(); //trim bc we dont want the beginning to be indented
		}

	}


	private static class SimpleTextTypePlugin implements ContentTypePlugin {

		@Override
		public boolean supportFormatting() {
			return false;
		}

		@Override
		public String getName() {
			return "test";
		}

		@Override
		public String getContentType() {
			return "application/test";
		}

		@Override
		public String formatContent(String text) {
			return text;
		}

		@Override
		public StyleSpans<Collection<String>> computeHighlighting(String text) {
			return new StyleSpansBuilder().add(Collections.emptyList(), text.length()).create();
		}

		@Override
		public boolean supportFolding() {
			return true;
		}

		@Override
		public ContentRange computeFolding(String text) {
			return parseText(text);
		}

		private void parseParagraph(CollapsableRange curRange, ContentRange prevRange, String paragraph) {

			String[] splits = StringUtils.split(paragraph, "()");
			if (splits.length == 1) {
				var containedNewLines = StringUtils.countMatches(paragraph, '\n');
				curRange.addChildren(new TextRange(prevRange, paragraph));
			} else {
				// for testing, assume, that there is one matching pair, e.g. 3 splits
				var split1Lines = StringUtils.countMatches(splits[0], '\n');
				var range1 = new TextRange(prevRange, splits[0]);
				curRange.addChildren(range1);

				var split2Lines = StringUtils.countMatches(splits[1], '\n');
				var nestedRange = new CollapsableRange(range1, false, "...\n");
				nestedRange.addChildren(new TextRange(range1, "(" + splits[1] + ")\n"));
				curRange.addChildren(nestedRange);

				var split3Lines = StringUtils.countMatches(splits[2], '\n');
				curRange.addChildren(new TextRange(nestedRange, splits[2].trim() + "\n"));
			}
		}

		protected CollapsableRange parseText(String text) {
			CollapsableRange rootRange = new CollapsableRange(null, true, "...\n");
			String[] splits = StringUtils.splitByWholeSeparator(text, "\n\n");
			ContentRange prevRange = null;

			for (int i = 0; i < splits.length; i++) {
				String split = splits[i];

				var curRange = new CollapsableRange(prevRange, false, "...\n");
				parseParagraph(curRange, prevRange, split + "\n");
				rootRange.addChildren(curRange);
				prevRange = curRange;
				if (i < splits.length - 1) { // not last one
					var paragraphSeparator = new TextRange(curRange, "\n");
					prevRange = paragraphSeparator;
					rootRange.addChildren(paragraphSeparator);
				}
			}
			return rootRange;
		}

	}
}