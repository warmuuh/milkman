package milkman.plugin.graphql;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import milkman.ui.plugin.ContentTypePlugin;

public class GraphqlContentType implements ContentTypePlugin {
	private static final String[] KEYWORDS = new String[] {
			"query", "mutation", "fragment", "on"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String GRAPHQL_KEYWORD = "(?<GRAPHQLKEYWORD>" + KEYWORD_PATTERN + ")";
    private static final String GRAPHQL_VALUE = "(?<=:)\\s*(?<GRAPHQLVALUE>[\\w]+)";
    private static final String GRAPHQL_VARIABLE = "(?<GRAPHQLVARIABLE>\\$[\\w]+)";
    private static final String GRAPHQL_DEFAULT = "(?<==)\\s*(?<GRAPHQLDEFAULT>[^,\\)]+)";


	    
	private static final String GRAPHQL_CURLY = "(?<GRAPHQLCURLY>\\{|\\})";
	private static final String GRAPHQL_ARRAY = "(?<GRAPHQLARRAY>\\[|\\])";
	private static final Pattern FINAL_REGEX = Pattern.compile(
			GRAPHQL_KEYWORD
			+ "|" + GRAPHQL_VALUE 
			+ "|" + GRAPHQL_VARIABLE
			+ "|" + GRAPHQL_DEFAULT
			);


	
	@Override
	public String getName() {
		return "Graphql";
	}

	@Override
	public String getContentType() {
		return "application/graphql";
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
		Matcher matcher = FINAL_REGEX.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("GRAPHQLKEYWORD") != null ? "keyword"
                    : matcher.group("GRAPHQLVALUE") != null ? "value"
                    : matcher.group("GRAPHQLVARIABLE") != null ? "keyword"
                    : matcher.group("GRAPHQLDEFAULT") != null ? "value"
					: matcher.group("GRAPHQLBOOL") != null ? "bool"
					: matcher.group("GRAPHQLNUMBER") != null ? "number"
                    : "plain";
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
	}

}
