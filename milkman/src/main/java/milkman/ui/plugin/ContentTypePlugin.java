package milkman.ui.plugin;

import milkman.ui.components.CodeFoldingContentEditor.ContentRange;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

/**
* extension point for content types used by the content-editor
*/
public interface ContentTypePlugin {

    /**
    * the name of the content, will show up in UI
    */
	String getName();

    /**
    * the mime-type of the content. e.g. rest plugin uses this to automatically
    * switch to the right content-type for a HttpResponse
    */
    String getContentType();

    /**
    * returns true if this content-type supports formatting
    */
	boolean supportFormatting();

    /**
    * returns the formatted version of the input text
    */
    String formatContent(String text);

    /**
    * calculates the highlighting of the given text.
    * see https://github.com/FXMisc/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/JavaKeywordsDemo.java for examples.
    * see milkman-syntax.css for style-classes that you can use in your highlighting.
    */
	StyleSpans<Collection<String>> computeHighlighting(String text);


	/**
	 * returns true if code folding is supported
	 */
	default boolean supportFolding() {
		return false;
	};
	
	/**
	 * returns hierarchical folding information
	 * @param text
	 * @return
	 */
	default ContentRange computeFolding(String text) {
		throw new UnsupportedOperationException();
	}


	default String computeIndentationForNextLine(String currentLine){
		return "";
	}
}
