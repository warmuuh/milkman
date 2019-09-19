package milkmancli.utils;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

	
	public static String stringToId(String value) {
		return Arrays.asList(value.split("[^a-zA-Z0-9]+"))
			.stream()
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.joining("-"));
	}
	
	public static Optional<String> findMatching(String idfiedName, List<String> realNames) {
		return realNames.stream()
				.filter(wsName -> stringToId(wsName).equals(idfiedName))
				.findAny();
	}
	
	public static <T> Optional<T> findMatching(String idfiedName, List<T> realNames, Function<T, String> toStringFn) {
		return realNames.stream()
				.filter(wsName -> stringToId(toStringFn.apply(wsName)).equals(idfiedName))
				.findAny();
	}
	
}
