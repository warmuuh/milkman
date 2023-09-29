package milkman.plugin.nosql.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import milkman.ui.plugin.ContentTypePlugin;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class NosqlContentType implements ContentTypePlugin {

		// https://github.com/eclipse/jnosql/blob/main/antlr4/org/eclipse/jnosql/query/grammar/Query.g4
    private static final String[] KEYWORDS = new String[] {
    		"select", "from", "delete", "insert", "update", "get", "del", "put",
				"\\*", "skip", "limit", "order", "by", "where",
				"between", "and", "in", "like", "not", "asc", "desc", "or",
				"day", "hour", "minute", "second", "millisecond", "nanosecond"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "--[^\n]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
);
    
    
	@Override
	public String getName() {
		return "Nosql";
	}

	@Override
	public String getContentType() {
		return "application/nosql";
	}

	@Override
	public boolean supportFormatting() {
		return false;
	}

	@Override
	public String formatContent(String text) {
		return null;
	}
	
	@Override
	public StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text.toLowerCase());
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "plain" :
                    matcher.group("BRACE") != null ? "object" :
                    matcher.group("BRACKET") != null ? "array" :
                    matcher.group("SEMICOLON") != null ? "plain" :
                    matcher.group("STRING") != null ? "value" :
                    matcher.group("COMMENT") != null ? "comment" :
                    "plain";
            spansBuilder.add(Collections.singleton("plain"), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
}
        spansBuilder.add(Collections.singleton("plain"), text.length() - lastKwEnd);
        return spansBuilder.create();
	}

}
