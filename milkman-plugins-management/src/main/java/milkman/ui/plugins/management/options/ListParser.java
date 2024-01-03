package milkman.ui.plugins.management.options;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.experimental.UtilityClass;

import static java.util.function.Predicate.not;

@UtilityClass
public class ListParser {

	public static List<String> parseList(String source) {
		return parseList(source, ",");
	}

	public static List<String> parseList(String source, String delimiter) {
		return Optional
			.ofNullable(source)
			.stream()
			.flatMap(s -> Arrays.stream(s.split(delimiter, 0)))
			.map(String::trim)
			.filter(not(String::isBlank))
			.toList();
	}
}
