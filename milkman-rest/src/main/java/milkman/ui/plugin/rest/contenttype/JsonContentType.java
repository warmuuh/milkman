package milkman.ui.plugin.rest.contenttype;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.components.CodeFoldingContentEditor;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.utils.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

	Pattern whiteSpace = Pattern.compile( "^\\s+" );

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

		StringWriter writer = new StringWriter();
		try {
			JsonFactory jsonFactory = new JsonFactory();
			try(BufferedReader br = new BufferedReader(new StringReader(text))) {
				Iterator<JsonNode> value = mapper.readValues( jsonFactory.createParser(br), JsonNode.class);
				value.forEachRemaining(u-> {
					try {
						boolean first = writer.getBuffer().length() == 0;
						if (!first) {
							writer.write("\n\n");
						}
						mapper.writeValue(writer, u);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}
			return writer.toString();
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
                    = matcher.group("JSONPROPERTY") != null ? "keyword"
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

	@Override
	public boolean supportFolding() {
		return true;
	}

	@Override
	public CodeFoldingContentEditor.ContentRange computeFolding(String text) {
		return parseText(text);
	}

	protected CodeFoldingContentEditor.CollapsableRange parseText(String text) {
		CodeFoldingContentEditor.CodeFoldingBuilder folding = new CodeFoldingContentEditor.CodeFoldingBuilder(text);

		char[] chars = text.toCharArray();
		int indentation = 0;

		for(int idx = 0; idx < chars.length; ++idx){
			char c = chars[idx];
			if(c == '{'){
				folding.startRange(idx, indent("{\n  ...\n}", indentation));
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

	@Override
	public String computeIndentationForNextLine(String currentLine) {
		Matcher m = whiteSpace.matcher(currentLine);
		String curIndentation = "";
		if (m.find()) {
			curIndentation = m.group();
		}
		var trim = currentLine.trim();
		if (trim.endsWith("{") || trim.endsWith("[")){
			curIndentation += "  ";
		}
		if (trim.endsWith("}") || trim.endsWith("]")){
			if (curIndentation.length() > 2){
				curIndentation = curIndentation.substring(0, curIndentation.length() - 2);
			}
		}
		return curIndentation;
	}

//
//	public static void main(String[] args) throws Exception {
//		JsonContentType contentType = new JsonContentType();
//		String bigJson = IOUtils.toString(new FileInputStream("/Users/peter.mucha/swork/big.json"));
//
//		Stopwatch.start("formatting");
//		contentType.formatContent(bigJson);
//		Stopwatch.stop("formatting");
//
//
//		Stopwatch.start("folding");
//		contentType.computeFolding(bigJson);
//		Stopwatch.stop("folding");
//
//
//		Stopwatch.start("formatting");
//		contentType.formatContent(bigJson);
//		Stopwatch.stop("formatting");
//
//
//
//		Stopwatch.start("highlight");
//		StyleSpans<Collection<String>> styleSpans = contentType.computeHighlighting(bigJson);
//		System.out.println(styleSpans.length());
//		Stopwatch.stop("highlight");
//
//		Stopwatch.start("fmthighlight");
//		StyleSpans<Collection<String>> styleSpans2 = contentType.computeHighlighting(contentType.formatContent(bigJson));
//		System.out.println(styleSpans2.length());
//		Stopwatch.stop("fmthighlight");
//
//	}
}
