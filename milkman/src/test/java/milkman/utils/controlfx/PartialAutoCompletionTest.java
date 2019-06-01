package milkman.utils.controlfx;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class PartialAutoCompletionTest {

	@Test
	void testRegex() {
		Pattern pattern = Pattern.compile("\\{\\{([^\\}]*)\\}\\}");
		Matcher matcher = pattern.matcher("this is a  {{test}} and {{another test}}");
		assertEquals(true, matcher.find());
		assertEquals("test", matcher.group(1));
		assertEquals(true, matcher.find());
		assertEquals("another test", matcher.group(1));
	}

}
