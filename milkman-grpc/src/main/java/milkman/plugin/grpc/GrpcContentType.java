package milkman.plugin.grpc;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import lombok.extern.slf4j.Slf4j;
import milkman.ui.plugin.ContentTypePlugin;

@Slf4j
public class GrpcContentType implements ContentTypePlugin {


	private static final String[] KEYWORDS = new String[] { 
			"double", "float", "int32", "int64", "uint32", "uint64",
			"sint32", "sint64", "fixed32", "fixed64", "sfixed32", 
			"sfixed64", "bool", "string", "bytes", "syntax",
			"option", "package", "message", "rpc", "stream", 
			"returns", "oneof", "repeated", "true", "false" };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "/\\*.*\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
);
    
    
	@Override
	public String getName() {
		return "proto";
	}

	@Override
	public String getContentType() {
		return "application/vnd.wrm.protoschema";
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
		Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "plain" :
                    matcher.group("BRACE") != null ? "object" :
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
