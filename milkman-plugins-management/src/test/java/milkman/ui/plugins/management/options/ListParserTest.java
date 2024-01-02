package milkman.ui.plugins.management.options;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ListParserTest {

	@ParameterizedTest
	@MethodSource
	@DisplayName("should parse lists correctly")
	void shouldParseListsCorrectly(String source, List<String> expected, String delimiter) {
		var result = ListParser.parseList(source, delimiter);

		assertThat(result).containsExactlyElementsOf(expected);
	}

	@Test
	@DisplayName("should parse list with default delimiter ,")
	void shouldParseListWithDefaultDelimiter() {
		var result = ListParser.parseList("a, b, c");

		assertThat(result).containsExactly("a", "b", "c");
	}

	static Stream<Arguments> shouldParseListsCorrectly() {
		return Stream
			.of(
				Arguments.of(null, List.of(), ","),
				Arguments.of("", List.of(), ","),
				Arguments.of(",", List.of(), ","),
				Arguments.of("a,,c", List.of("a", "c"), ","),
				Arguments.of("a, b, c,d,  e , f, ; and so much more", List.of("a", "b", "c", "d", "e", "f", "; and so much more"), ","),
				Arguments.of("a; b; c;d;  e ; f; , and so much more", List.of("a", "b", "c", "d", "e", "f", ", and so much more"), ";")
			);
	}

}
