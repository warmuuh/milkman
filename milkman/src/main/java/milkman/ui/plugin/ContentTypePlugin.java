package milkman.ui.plugin;

import java.util.Collection;

import org.fxmisc.richtext.model.StyleSpans;

public interface ContentTypePlugin {

	String getName();
	String getContentType();
	
	boolean supportFormatting();
	String formatContent(String text);
	
	StyleSpans<Collection<String>> computeHighlighting(String text);
}
