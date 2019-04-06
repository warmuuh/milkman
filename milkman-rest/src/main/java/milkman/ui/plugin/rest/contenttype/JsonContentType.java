package milkman.ui.plugin.rest.contenttype;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;
import milkman.ui.plugin.ContentTypePlugin;

/**
 * highlighting copied from
 * https://github.com/RohitAwate/Everest/blob/master/src/main/java/com/rohitawate/everest/controllers/codearea/highlighters/JSONHighlighter.java
 * Copyright 2018 Rohit Awate.
 * 
 * 
 * 
 * changes: 
 * non-highlighting related stuff
 * css classes
 * 
 *
 */
@Slf4j
public class JsonContentType implements ContentTypePlugin {

	private static final String JSON_CURLY = "(?<JSONCURLY>\\{|\\})";
	private static final String JSON_PROPERTY = "(?<JSONPROPERTY>\\\"[^\\\"]+\\\")\\s*:";
	private static final String JSON_VALUE = ":\\s*(?<JSONVALUE>\\\"[^\\\"]+\\\")";
	private static final String JSON_ARRAY = "(?<JSONARRAY>\\[|\\])";
	private static final String JSON_NUMBER = "(?<JSONNUMBER>\\d*.?\\d*)";
	private static final String JSON_BOOL = "(?<JSONBOOL>true|false)";

	private static final Pattern FINAL_REGEX = Pattern.compile(JSON_CURLY + "|" + JSON_PROPERTY + "|" + JSON_VALUE + "|"
			+ JSON_ARRAY + "|" + JSON_BOOL + "|" + JSON_NUMBER);

	@Override
	public String getName() {
		return "Json";
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public boolean supportFormatting() {
		return true;
	}

	@Override
	public String formatContent(String text) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			JsonNode node = mapper.readTree(text);
			return mapper.writeValueAsString(node);
		} catch (Throwable t) {
			log.warn("failed to format json", t);
		}
		return text;
	}

	@Override
	public StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = FINAL_REGEX.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("JSONPROPERTY") != null ? "property"
                    : matcher.group("JSONVALUE") != null ? "value"
                    : matcher.group("JSONARRAY") != null ? "array"
                    : matcher.group("JSONCURLY") != null ? "object"
                    : matcher.group("JSONBOOL") != null ? "bool"
                    : matcher.group("JSONNUMBER") != null ? "number"
                    : "plain";
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
	}

}
