package milkman.ui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;

import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.ui.components.CodeFoldingContentEditor.CollapsableRange;
import milkman.ui.components.CodeFoldingContentEditor.ContentRange;
import milkman.ui.components.CodeFoldingContentEditor.TextRange;
import milkman.ui.components.LineIndicatorDemo.ContentBean;
import milkman.ui.plugin.ContentTypePlugin;

/**
 * Demonstrates the usage of
 * {@link StyledTextArea#paragraphGraphicFactoryProperty()}.
 */
public class LineIndicatorDemo extends Application {

	private CodeFoldingContentEditor codeArea;

	@Data
	@AllArgsConstructor
	public static class ContentBean {
		String text;
	}

	@Override
	public void start(Stage primaryStage) {
		codeArea = new CodeFoldingContentEditor();

		codeArea.setContentTypePlugins(Arrays.asList(new ContentTypePlugin() {

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
					var nestedRange = new CollapsableRange(range1, false);
					nestedRange.addChildren(new TextRange(range1, "(" + splits[1] + ")\n"));
					curRange.addChildren(nestedRange);

					var split3Lines = StringUtils.countMatches(splits[2], '\n');
					curRange.addChildren(new TextRange(nestedRange, splits[2].trim() + "\n"));
				}
			}

			protected CollapsableRange parseText(String text) {
				CollapsableRange rootRange = new CollapsableRange(null, true);
				String[] splits = StringUtils.splitByWholeSeparator(text, "\n\n");
				ContentRange prevRange = null;

				for (int i = 0; i < splits.length; i++) {
					String split = splits[i];

					var curRange = new CollapsableRange(prevRange, false);
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

		}));

		codeArea.setContentType("application/test");
		codeArea.setEditable(false);
		ContentBean bean = new ContentBean("This is a paragraph of text.\n"
				+ "(This is a nested collapsable paragraph of text.\n" + "This is a paragraph of text.)\n"
				+ "This is a paragraph of text.\n" + "\n" + "This is a paragraph of text.\n"
				+ "This is a paragraph of text.\n" + "This is a paragraph of text.\n" + "This is a paragraph of text.\n"
				+ "\n" + "This is a paragraph of text.\n" + "This is a paragraph of text.\n"
				+ "This is a paragraph of text.\n" + "This is a paragraph of text.\n" + "\n" + "Try it.");

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