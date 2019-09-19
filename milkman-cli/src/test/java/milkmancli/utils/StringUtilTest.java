package milkmancli.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ArgumentsSources;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StringUtilTest {

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
		"test string|test-string",
		"test (string)|test-string",
		"(test) (string)|test-string",
		"test-string|test-string",
		"test -string|test-string"
	})
	void shouldConvertStringsToId(String input, String expected) {
		assertEquals(expected, StringUtil.stringToId(input));
	}

}
