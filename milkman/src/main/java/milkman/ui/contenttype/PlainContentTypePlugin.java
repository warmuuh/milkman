package milkman.ui.contenttype;

import java.util.Collection;
import java.util.Collections;


import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import lombok.SneakyThrows;
import lombok.val;
import milkman.ui.plugin.ContentTypePlugin;

public class PlainContentTypePlugin implements ContentTypePlugin {

	@Override
	public String getName() {
		return "Plain Text";
	}

	@Override
	public boolean supportFormatting() {
		return false;
	}

	@Override
	@SneakyThrows
	public String formatContent(String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StyleSpans<Collection<String>> computeHighlighting(String text) {
		val b = new StyleSpansBuilder<Collection<String>>();
		b.add(Collections.singleton("plain"), text.length());
		return b.create();
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

}
