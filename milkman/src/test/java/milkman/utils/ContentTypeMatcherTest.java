package milkman.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeMatcherTest {

    @ParameterizedTest
    @CsvSource({
            "application/json, application/json, true",
            "application/json, application/json; param=123, true",
            "application/json, application/blubber+json, true",
            "application/json+blubber, application/json, false",
            "application/json, application/html, false"
    })
    public void shouldMatchSubtypes(String contentType, String contenTypeToMatch, boolean matching) {
        assertThat(ContentTypeMatcher.matches(contentType, contenTypeToMatch)).isEqualTo(matching);
    }

}